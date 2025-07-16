package inu.codin.codin.domain.report.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.comment.service.CommentService;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.domain.reply.service.ReplyCommentService;
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import inu.codin.codin.domain.post.entity.PostAnonymous;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.service.PostService;
import inu.codin.codin.domain.report.dto.ReportInfo;
import inu.codin.codin.domain.report.dto.request.ReportCreateRequestDto;
import inu.codin.codin.domain.report.dto.request.ReportExecuteRequestDto;
import inu.codin.codin.domain.report.dto.response.*;
import inu.codin.codin.domain.report.entity.*;
import inu.codin.codin.domain.report.exception.ReportAlreadyExistsException;
import inu.codin.codin.domain.report.exception.ReportUnsupportedTypeException;
import inu.codin.codin.domain.report.repository.CustomReportRepository;
import inu.codin.codin.domain.report.repository.ReportRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    private final PostService postService;
    private final CommentService commentService;
    private final ReplyCommentService replyCommentService;

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyCommentRepository replyRepository;
    private final ReplyCommentRepository replyCommentRepository;
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








    @Transactional
    public void handleReport(ReportExecuteRequestDto requestDto) {
        log.info("신고 처리 요청: {}", requestDto.getReportTargetId());
        //현재 관리자 ID
        ObjectId userId = SecurityUtils.getCurrentUserId();
        ObjectId targetObjectId = new ObjectId(requestDto.getReportTargetId());

        // 해당 신고 대상에 대한 모든 신고 가져오기
        List<ReportEntity> reports = reportRepository.findByReportTargetId(targetObjectId);
        if (reports.isEmpty()) {
            throw new NotFoundException("해당 신고 대상에 대한 신고가 존재하지 않습니다. 대상 ID: " + targetObjectId);
        }

        // 처리되지 않은 신고만!
        List<ReportEntity> pendingReports = reports.stream()
                .filter(report -> report.getReportStatus() == ReportStatus.PENDING)
                .toList();

        if (pendingReports.isEmpty()) {
            throw new ReportAlreadyExistsException("이미 처리된 신고입니다.");
        }

        //  정지 종료일 계산
        LocalDateTime suspensionEndDate = (requestDto.getSuspensionPeriod() == SuspensionPeriod.PERMANENT)
                ? LocalDateTime.of(9999, 12, 31, 23, 59)
                : LocalDateTime.now().plusDays(requestDto.getSuspensionPeriod().getDays());

        //  신고 처리 정보 생성
        ReportEntity.ReportActionEntity action = ReportEntity.ReportActionEntity.builder()
                .actionTakenById(userId)
                .suspensionPeriod(requestDto.getSuspensionPeriod())
                .suspensionEndDate(suspensionEndDate)
                .build();

        // 신고 상태 업데이트 (모든 관련 신고 SUSPENDED로 변경)
        pendingReports.forEach(report -> report.updateReportSuspended(action));

        // 신고 대상 삭제 처리 (Soft Delete)
        ReportTargetType targetType = reports.get(0).getReportTargetType();
        log.info("신고 대상 삭제 처리: 대상 ID: {}, 대상 타입: {}", targetObjectId, targetType);
        deleteReportedEntity(targetObjectId, targetType);


        //유저 Suspended - 정지 상태로 변경
        ObjectId reportedUserId = reports.get(0).getReportedUserId();
        UserEntity user = userRepository.findById(reportedUserId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        //유저 정지시키기
        user.suspendUser();
        user.updateTotalSuspensionEndDate(
                (requestDto.getSuspensionPeriod().equals(SuspensionPeriod.PERMANENT))
                        ? LocalDateTime.of(9999, 12, 30, 23, 59)
                        : Objects.requireNonNullElseGet(user.getTotalSuspensionEndDate(), LocalDateTime::now)
                        .plusDays(requestDto.getSuspensionPeriod().getDays())
        );

        // 업데이트된 신고 저장
        reportRepository.saveAll(pendingReports);
        userRepository.save(user);
        log.info(" 신고 처리 완료: 신고 대상 ID: {}, reportedUserId: {}", requestDto.getReportTargetId(), reportedUserId);

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
            default -> throw new ReportUnsupportedTypeException("잘못된 신고 대상 타입입니다.");
        }
    }

    public void resolveReport(String reportTargetId) {
        log.info(" 신고대상 유지 요청: 신고 ID: {}", reportTargetId);

        ObjectId targetObjectId = new ObjectId(reportTargetId);
        ObjectId userId = SecurityUtils.getCurrentUserId(); // 현재 유저 ID

        //  신고 존재 확인
        List<ReportEntity> reports = reportRepository.findByReportTargetId(targetObjectId);

        if (reports.isEmpty()) {
            throw new NotFoundException("해당 신고 대상에 대한 신고가 존재하지 않습니다. 대상 ID: " + reportTargetId);
        }

        //이미 RESOLVED 또는 SUSPENDED 상태인지 확인 후, 처리되지 않은 신고만 변경
        reports.stream()
                .filter(report -> report.getReportStatus() == ReportStatus.PENDING)
                .forEach(report -> {
                    report.ReportResolved(userId); // 신고 상태를 `RESOLVED`로 변경
                    log.info(" 신고 유지 처리 완료: 신고 ID: {}", report.get_id());
                });

        reportRepository.saveAll(reports);

        log.info("총 {}개의 신고가 유지 처리되었습니다. 대상 ID: {}", reports.size(), reportTargetId);
    }


    /***
     *
     * 신고 조회 로직
     *
     */

    public ReportPageResponse getAllReportedPosts(int pageNumber) {
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());

        List<ReportInfo> reportInfos = reportRepository.findAllReportedEntities();

        // 페이지 변환
        int start = Math.min((int) pageRequest.getOffset(), reportInfos.size());
        int end = Math.min((start + pageRequest.getPageSize()), reportInfos.size());
        Page<ReportInfo> reportInfoPage = new PageImpl<>(reportInfos.subList(start, end), pageRequest, reportInfos.size());

        // 신고된 엔터티 조회 및 변환
        List<ReportListResponseDto> reportedPosts = reportInfoPage.getContent().stream()
                .map(this::getReportedPostDetail)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        long lastPage = reportInfoPage.getTotalPages() - 1;
        long nextPage = reportInfoPage.hasNext() ? pageNumber + 1 : -1;

        return ReportPageResponse.of(reportedPosts, lastPage, nextPage);
    }

    private Optional<ReportListResponseDto> getReportedPostDetail(ReportInfo reportInfo) {
        ObjectId entityId = new ObjectId(reportInfo.getReportedEntityId());

        return switch (reportInfo.getEntityType()) {
            case POST -> postService.getPostDetailById(entityId) // ✅ PostService 활용
                    .map(postDTO -> ReportListResponseDto.from(postDTO, reportInfo));

            case COMMENT -> commentRepository.findById(entityId)
                    .flatMap(comment -> postService.getPostDetailById(comment.getPostId())
                            .map(postDTO -> ReportListResponseDto.from(postDTO, reportInfo)));

            case REPLY -> replyCommentRepository.findById(entityId)
                    .flatMap(reply -> commentRepository.findById(reply.getCommentId())
                            .flatMap(comment -> postService.getPostDetailById(comment.getPostId())
                                    .map(postDTO -> ReportListResponseDto.from(postDTO, reportInfo))));

            default -> Optional.empty();
        };
    }

    public ReportedPostDetailResponseDTO getReportedPostWithDetail(String postId, String reportedEntityId) {
        ObjectId entityId = new ObjectId(postId);
        ObjectId reportTargetId = new ObjectId(reportedEntityId);

        // 게시글이 신고된 경우 표시 추가
        boolean existsInReportDB = reportRepository.existsByReportTargetId(reportTargetId);
        if (!existsInReportDB) {
            throw new NotFoundException("해당 신고 대상이 존재하지 않습니다. 신고 ID: " + reportedEntityId);
        }
        PostDetailResponseDTO postDetailResponse = postService.getPostDetailById(entityId)
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));
        boolean isReported = entityId.equals(reportTargetId);


        return ReportedPostDetailResponseDTO.from(isReported, postDetailResponse);
    }


    public List<ReportedCommentDetailResponseDTO> getReportedCommentsByPostId(String postId, String reportedEntityId) {
        List<CommentResponseDTO> comments = commentService.getCommentsByPostId(postId);
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));

        return comments.stream()
                .map(comment -> {
                    ObjectId ReportTargetId = new ObjectId(reportedEntityId);
                    boolean existsInReportDB = reportRepository.existsByReportTargetId(ReportTargetId);
                    boolean isCommentReported = existsInReportDB && comment.get_id().equals(reportedEntityId);
                    log.info("🔸 댓글 ID: {}, 신고 여부: {}", comment.get_id(), isCommentReported);

                    // 대댓글 리스트 변환 (신고 여부 반영)
                    List<ReportedCommentDetailResponseDTO> reportedReplies = getReportedRepliesByCommentId(post.getAnonymous(), comment.get_id(), reportedEntityId);

                    // `CommentResponseDTO`에서 `ReportedCommentResponseDTO`로 변환하여 신고 여부 추가
                    return ReportedCommentDetailResponseDTO.from(comment.repliesFrom(reportedReplies), isCommentReported);
                })
                .toList();
    }

    public List<ReportedCommentDetailResponseDTO> getReportedRepliesByCommentId(PostAnonymous postAnonymous, String id, String reportedEntityId) {
        ObjectId commentId = new ObjectId(id);
        List<CommentResponseDTO> replies = replyCommentService.getRepliesByCommentId(postAnonymous, commentId);

        return replies.stream()
                .map(reply -> {
                    ObjectId ReportTargetId = new ObjectId(reportedEntityId);
                    boolean existsInReportDB = reportRepository.existsByReportTargetId(ReportTargetId);
                    boolean isReplyReported = existsInReportDB && reply.get_id().equals(reportedEntityId);

                    log.info("🔹 대댓글 ID: {}, 신고 여부: {}", reply.get_id(), isReplyReported);

                    return ReportedCommentDetailResponseDTO.from(reply, isReplyReported);
                })
                .toList();
    }
}

