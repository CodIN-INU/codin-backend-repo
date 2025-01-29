package inu.codin.codin.domain.report.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.report.dto.request.ReportCreateRequestDto;
import inu.codin.codin.domain.report.dto.request.ReportExecuteRequestDto;
import inu.codin.codin.domain.report.dto.response.ReportResponseDto;
import inu.codin.codin.domain.report.entity.ReportEntity;
import inu.codin.codin.domain.report.entity.ReportStatus;
import inu.codin.codin.domain.report.entity.ReportTargetType;
import inu.codin.codin.domain.report.entity.SuspensionPeriod;
import inu.codin.codin.domain.report.exception.ReportAlreadyExistsException;
import inu.codin.codin.domain.report.repository.ReportRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.exception.UserCreateFailException;
import inu.codin.codin.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyCommentRepository replyRepository;


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

        log.info("신고 유저 검증 완료: userId={}", userId);

        // 중복 신고 방지
        log.info("중복 신고 검증: reportingUserId={}, reportTargetId={}, reportTargetType={}",
                userId,
                reportCreateRequestDto.getReportTargetId(),
                reportCreateRequestDto.getReportTargetType());

        Boolean reportExists = reportRepository.existsByReportingUserIdAndReportTargetIdAndReportTargetType(
                userId,
                reportTargetId,
                reportCreateRequestDto.getReportTargetType()
        );

        // null 방지: reportExists가 null이면 false로 처리
        if (Boolean.TRUE.equals(reportExists)) {
            log.warn("중복 신고 발견: reportingUserId={}, reportTargetId={}, reportTargetType={}",
                    userId,
                    reportCreateRequestDto.getReportTargetId(),
                    reportCreateRequestDto.getReportTargetType());
            throw new ReportAlreadyExistsException("중복신고 : 이미 해당 대상에 대한 신고를 시행했습니다.");
        }

        // 신고 대상 타입 별 유효성 검증
        log.info("신고 대상 유효성 검증: reportTargetType={}, reportTargetId={}",
                reportCreateRequestDto.getReportTargetType(),
                reportCreateRequestDto.getReportTargetId());

        // 신고 대상 검증 및 userId 가져오기
        ObjectId reportedUserId = validateAndGetReportedUserId(reportCreateRequestDto.getReportTargetType(), reportTargetId);

        log.info("신고 대상 유효성 검증 완료: reportTargetType={}, reportTargetId={}",
                reportCreateRequestDto.getReportTargetType(),
                reportCreateRequestDto.getReportTargetId());

        // 신고 엔티티 생성
        ReportEntity report = ReportEntity.builder()
                .reportingUserId(userId)
                .reportedUserId(reportedUserId)
                .reportTargetId(reportTargetId)
                .reportTargetType(reportCreateRequestDto.getReportTargetType())
                .reportType(reportCreateRequestDto.getReportType())
                .build();

        log.info("신고 엔티티 생성 완료: {}", report);

        // 신고 저장
        reportRepository.save(report);
        log.info("신고 저장 완료: reportId={}, reportingUserId={}, reportTargetId={}",
                report.get_id(),
                userId,
                reportCreateRequestDto.getReportTargetId());
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

        log.info("신고 대상 검증 및 userId 조회: reportTargetType={}, reportTargetId={}", reportTargetType, reportTargetId);

        // 검증 및 userId 조회
        return Optional.ofNullable(validators.get(reportTargetType))
                .flatMap(validator -> validator.apply(reportTargetId))
                .orElseThrow(() -> {
                    log.error("유효하지 않은 신고 대상: reportTargetId={}, reportTargetType={}", reportTargetId, reportTargetType);
                    return new NotFoundException("신고 대상(ID: " + reportTargetId + ", Type: " + reportTargetType + ")이 존재하지 않습니다.");
                });
    }




    // 신고 목록 조회 (관리자)
    public List<ReportResponseDto> getAllReports(ReportTargetType reportTargetType, Integer minReportCount) {
        log.info("신고 목록 조회: reportType={}, minReportCount={}", reportTargetType, minReportCount);

        List<ReportEntity> reports;
        if (reportTargetType != null) {
            reports = reportRepository.findByReportTargetType(reportTargetType);
        } else if (minReportCount != null) {
            reports = reportRepository.findReportsByMinReportCount(minReportCount);
        } else {
            reports = reportRepository.findAll();
        }

        // ReportEntity를 ReportResponseDto로 변환
        return reports.stream()
                .map(ReportResponseDto::from)
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



    public void executeReport(ReportExecuteRequestDto requestDto) {
        log.info("신고 처리 요청: {}", requestDto.getReportId());
        ObjectId userId = SecurityUtils.getCurrentUserId();
        ObjectId ReportId = new ObjectId(requestDto.getReportId());


        // 신고 존재 확인
        ReportEntity report = reportRepository.findById(ReportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고가 존재하지 않습니다. ID: " + requestDto.getReportId()));

        // 이미 처리된 신고인지 확인
        if (report.getReportStatus() == ReportStatus.RESOLVED) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        // 신고 처리 정보 생성
        ReportEntity.ReportActionEntity action = ReportEntity.ReportActionEntity.builder()
                .actionTakenById(userId)
                .comment(requestDto.getComment())
                .suspensionPeriod(requestDto.getSuspensionPeriod())
                .suspensionEndDate(requestDto.getSuspensionEndDate())
                .build();

        // 엔티티 내부에서 업데이트 메서드 호출
        //ReportEntity updatedReport = report.updateReport(action);

        // 기존 객체의 필드를 직접 수정 (새 객체 생성 X)
        report.updateReportResolved(action);


        //유저 Suspended - 정지 상태로 변경
        Optional<UserEntity> user = userRepository.findById(report.getReportedUserId());
        if (user.isEmpty()) throw new NotFoundException("존재하지 않는 회원입니다.");
        //영구 정지
        if (requestDto.getSuspensionPeriod() == SuspensionPeriod.PERMANENT){
            user.get().disabledUser();
        } else {
            user.get().suspendUser();
        }


        // 업데이트된 신고 저장
        reportRepository.save(report);
        userRepository.save(user.get());
        //userRepository.save(user);
        log.info("신고가 처리되었습니다. ID: {}, 처리자: {}", report.get_id(), userId);

        ReportResponseDto.from(report);
    }






}

