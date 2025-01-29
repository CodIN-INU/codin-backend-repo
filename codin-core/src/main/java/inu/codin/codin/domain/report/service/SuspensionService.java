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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuspensionService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public void releaseSuspendedUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<ReportEntity> suspendedUsers = reportRepository.findSuspendedUsers(now);

        for (ReportEntity report : suspendedUsers) {
            Optional<UserEntity> userOpt = userRepository.findById(report.getReportedUserId());
            if (userOpt.isEmpty()) continue;

            UserEntity user = userOpt.get();
            user.activateUser(); // 정지 해제
            userRepository.save(user); // DB 반영

            log.info("유저 {} 정지 해제 완료", user.get_id());
        }
    }
}
