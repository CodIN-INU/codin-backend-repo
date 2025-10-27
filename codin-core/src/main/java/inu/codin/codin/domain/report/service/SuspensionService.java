package inu.codin.codin.domain.report.service;

import inu.codin.codin.domain.report.entity.ReportEntity;
import inu.codin.codin.domain.report.repository.ReportRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuspensionService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public void releaseSuspendedReports() {
        LocalDateTime now = LocalDateTime.now();
        List<ReportEntity> suspendedUsers = reportRepository.findSuspendedReports(now);

        for (ReportEntity report : suspendedUsers) {
            report.updateReportResolved();
            reportRepository.save(report);
            log.info("신고 {} 정지 중 -> 처리 완료", report.get_id());
        }
    }

    public void releaseSuspendedUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<UserEntity> userEntities = userRepository.findSuspendedUsers(now);
        for (UserEntity user : userEntities) {
            user.activateUser(); // 정지 해제
            user.updateTotalSuspensionEndDate(null);
            userRepository.save(user); // DB 반영
            log.info("유저 {} 정지 해제", user.get_id());
        }
    }
}
