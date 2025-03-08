package inu.codin.codin.domain.report.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.block.service.BlockService;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.report.dto.request.ReportCreateRequestDto;
import inu.codin.codin.domain.report.dto.request.ReportExecuteRequestDto;
import inu.codin.codin.domain.report.dto.response.ReportCountResponseDto;
import inu.codin.codin.domain.report.dto.response.ReportResponseDto;
import inu.codin.codin.domain.report.dto.response.ReportSummaryResponseDTO;
import inu.codin.codin.domain.report.entity.*;
import inu.codin.codin.domain.report.exception.ReportAlreadyExistsException;
import inu.codin.codin.domain.report.repository.CustomReportRepository;
import inu.codin.codin.domain.report.repository.ReportRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final BlockService blockService;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyCommentRepository replyRepository;
    private final CustomReportRepository customReportRepository;


    public void createReport(@Valid ReportCreateRequestDto reportCreateRequestDto) {
        /***
         * User 검증
         * 중복신고 방지
         * 신고 대상 유효성 검증 (reportTargetId가 유효한 대상을 참조)
         */
        log.info("신고 생성 요청 시작: {} ", reportCreateRequestDto);

        // 신고한 유저 검증
        ObjectId userId = SecurityUtils.getCurrentUserId();
        ObjectId reportTargetId = new ObjectId(reportCreateRequestDto.getReportTargetId());


        boolean reportExists = reportRepository.existsByReportingUserIdAndReportTargetIdAndReportTargetType(
                userId,
                reportTargetId,
                reportCreateRequestDto.getReportTargetType()
        );

        if (reportExists) {
            log.warn("중복 신고 발견: reportingUserId={}, reportTargetId={},",
                    userId,
                    reportCreateRequestDto.getReportTargetId());
            throw new ReportAlreadyExistsException("중복신고 : 이미 해당 대상에 대한 신고를 시행했습니다.");
        }

        // 신고 대상 검증 및 userId 가져오기
        ObjectId reportedUserId = validateAndGetReportedUserId(reportCreateRequestDto.getReportTargetType(), reportTargetId);

        // 신고 엔티티 생성
        ReportEntity report = ReportEntity.builder()
                .reportingUserId(userId)
                .reportedUserId(reportedUserId)
                .reportTargetId(reportTargetId)
                .reportTargetType(reportCreateRequestDto.getReportTargetType())
                .reportType(reportCreateRequestDto.getReportType())
                .build();

        // 신고 저장
        reportRepository.save(report);
        log.info("신고 저장 완료: reportId={}, reportingUserId={}, reportTargetId={}",
                report.get_id(),
                userId,
                reportCreateRequestDto.getReportTargetId());

        // 신고 대상 타입 == POST(게시물) reportCount 증가
        if (reportCreateRequestDto.getReportTargetType() == ReportTargetType.POST) {
            updatePostReportCount(reportTargetId);
        }
    }


    //post 총 신고수 증가
    private void updatePostReportCount(ObjectId postId) {
        // 게시물 조회
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));

        // 신고 수 업데이트
        post.updateReportCount(post.getReportCount() + 1);

        // 게시물 저장
        postRepository.save(post);
    }

    /**
     * 신고 대상 유효성 검증 및 신고 대상 UserId 추출
     */
    private ObjectId validateAndGetReportedUserId(ReportTargetType reportTargetType, ObjectId reportTargetId) {
        // 타입별 유효성 검증 로직을 Map으로 관리
        Map<ReportTargetType, Function<ObjectId, Optional<ObjectId>>> validators = Map.of(
                ReportTargetType.USER, Optional::of, // User의 경우, ID 자체가 신고 대상
                ReportTargetType.POST, id -> postRepository.findById(id).map(PostEntity::getUserId),
                ReportTargetType.COMMENT, id -> commentRepository.findById(id).map(CommentEntity::getUserId),
                ReportTargetType.REPLY, id -> replyRepository.findById(id).map(ReplyCommentEntity::getUserId)
        );

        // 검증 및 userId 조회
        return Optional.ofNullable(validators.get(reportTargetType))
                .flatMap(validator -> validator.apply(reportTargetId))
                .orElseThrow(() -> {
                    log.warn("유효하지 않은 신고 대상: reportTargetId={}, reportTargetType={}", reportTargetId, reportTargetType);
                    return new NotFoundException("신고 대상(ID: " + reportTargetId + ", Type: " + reportTargetType + ")이 존재하지 않습니다.");
                });
    }





    // 신고 목록 조회 (관리자)

        public List<ReportCountResponseDto> getAllReports() {

            List<Document> aggregationResults = customReportRepository.findPendingReportsOrderedGroupedBy();

             return aggregationResults.stream()
                    .map(ReportCountResponseDto::from)  // 변환 메서드 호출
                     .collect(Collectors.toList());
        }



    // 특정 유저가 신고 내역 조회 (관리자)
    public List<ReportResponseDto> getReportsByUserId(String userId) {
        log.info("특정 유저 신고 내역 조회: userId={}", userId);

        ObjectId ObjUserId = new ObjectId(userId);
        List<ReportEntity> reports = reportRepository.findByReportingUserId(ObjUserId);

        log.info("DB에서 가져온 ReportEntity 리스트:");
        reports.forEach(report -> log.info("ID: {}, ReportStatus: {}", report.get_id(), report.getReportStatus()));

        return reports.stream()
                .map(ReportResponseDto::from)
                .collect(Collectors.toList());
    }



    @Transactional
    public void handleReport(ReportExecuteRequestDto requestDto) {
        log.info("신고 처리 요청: {}", requestDto.getReportId());
        //현재 관리자 ID
        ObjectId userId = SecurityUtils.getCurrentUserId();
        ObjectId ReportId = new ObjectId(requestDto.getReportId());

        // 신고 존재 확인
        ReportEntity report = reportRepository.findById(ReportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고가 존재하지 않습니다. ID: " + requestDto.getReportId()));

        // 이미 처리된 신고인지 확인
        if (report.getReportStatus() == ReportStatus.SUSPENDED || report.getReportStatus() == ReportStatus.RESOLVED) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        // 신고 처리 정보 생성
        ReportEntity.ReportActionEntity action = null;
        if (requestDto.getSuspensionPeriod().equals(SuspensionPeriod.PERMANENT)){
            action = ReportEntity.ReportActionEntity.builder()
                    .actionTakenById(userId)
                    .suspensionPeriod(requestDto.getSuspensionPeriod())
                    .suspensionEndDate(LocalDateTime.of(9999,12,31,23,59))
                    .build();
        }
        else action = ReportEntity.ReportActionEntity.builder()
                .actionTakenById(userId)
                .suspensionPeriod(requestDto.getSuspensionPeriod())
                .suspensionEndDate(LocalDateTime.now().plusDays(requestDto.getSuspensionPeriod().getDays()))
                .build();

        // 기존 객체의 필드를 직접 수정 (새 객체 생성 X)
        report.updateReportSuspended(action);

        //유저 Suspended - 정지 상태로 변경
        Optional<UserEntity> user = userRepository.findById(report.getReportedUserId());
        if (user.isEmpty()) throw new NotFoundException("존재하지 않는 회원입니다.");

        user.get().suspendUser();
        //영구 정지
        if (requestDto.getSuspensionPeriod() == SuspensionPeriod.PERMANENT){
            user.get().updateTotalSuspensionEndDate(LocalDateTime.of(9999, 12 ,30, 23, 59));
        } else {
            LocalDateTime totalSuspensionEndDate = user.get().getTotalSuspensionEndDate();
            user.get().updateTotalSuspensionEndDate(
                    Objects.requireNonNullElseGet(totalSuspensionEndDate, LocalDateTime::now)
                            .plusDays(requestDto.getSuspensionPeriod().getDays()));
        }

        deleteReportedEntity(report.getReportTargetId(), report.getReportTargetType());
        // 업데이트된 신고 저장
        reportRepository.save(report);
        userRepository.save(user.get());
        log.info("신고가 처리되었습니다. ID: {}, 처리자: {}", report.get_id(), userId);

    }



    public ReportSummaryResponseDTO getReportSummary(String reportTargetId) {
        ObjectId targetId = new ObjectId(reportTargetId);

        // 모든 ReportType에 대해 개수 조회
        Map<ReportType, Integer> reportTypeCounts = new HashMap<>();
        for (ReportType reportType : ReportType.values()) {
            int count =  reportRepository.countByReportTargetIdAndReportType(targetId, reportType);
            if (count > 0) { // 개수가 0이면 굳이 넣을 필요 없음
                reportTypeCounts.put(reportType, count);
            }
        }

        return new ReportSummaryResponseDTO(reportTypeCounts);
    }

    @Transactional
    public void deleteReportedEntity(ObjectId reportTargetId, ReportTargetType targetType) {

        switch (targetType) {
            case POST -> {
                PostEntity post = postRepository.findById(reportTargetId)
                        .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));
                post.delete();
                postRepository.save(post);
                log.info(" 신고된 게시글 삭제: {}", reportTargetId);
            }
            case COMMENT -> {
                CommentEntity comment = commentRepository.findById(reportTargetId)
                        .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
                comment.delete();
                commentRepository.save(comment);
                log.info(" 신고된 댓글 삭제: {}", reportTargetId);
            }
            case REPLY -> {
                ReplyCommentEntity reply = replyRepository.findById(reportTargetId)
                        .orElseThrow(() -> new NotFoundException("대댓글을 찾을 수 없습니다."));
                reply.delete();
                replyRepository.save(reply);
                log.info("신고된 대댓글 삭제: {}", reportTargetId);
            }
            default -> throw new IllegalArgumentException("잘못된 신고 대상 타입입니다.");
        }
    }

    public void resolveReport(String reportId) {
        log.info(" 신고대상 유지 요청: 신고 ID: {}", reportId);

        ObjectId reportObjectId = new ObjectId(reportId);
        ObjectId userId = SecurityUtils.getCurrentUserId(); // 현재 유저 ID

        //  신고 존재 확인
        ReportEntity report = reportRepository.findById(reportObjectId)
                .orElseThrow(() -> new NotFoundException("해당 신고가 존재하지 않습니다. ID: " + reportId));

        //  이미 처리된 신고인지 확인 (RESOLVED, SUSPENDED 상태면 예외 발생)
        if (report.getReportStatus() == ReportStatus.RESOLVED || report.getReportStatus() == ReportStatus.SUSPENDED) {
            throw new IllegalStateException("이미 처리된 신고입니다. 상태: " + report.getReportStatus());
        }

        // 신고 상태를 `RESOLVED`로 변경
        report.ReportResolved(userId);

        // 변경된 신고 저장
        reportRepository.save(report);

        log.info(" 신고 유지 완료: 신고 ID: {}, 처리자: {}", report.get_id(), userId);
    }
}

