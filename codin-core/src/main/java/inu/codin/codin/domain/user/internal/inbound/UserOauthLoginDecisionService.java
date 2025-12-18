package inu.codin.codin.domain.user.internal.inbound;

import inu.codin.codin.domain.user.internal.inbound.dto.OauthDecisionRequest;
import inu.codin.codin.domain.user.internal.inbound.dto.OauthDecisionResponse;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.entity.UserStatus;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;
import inu.codin.common.dto.Department;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserOauthLoginDecisionService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    public OauthDecisionResponse decideOauthAction(OauthDecisionRequest request) {
        return switch (request.provider()) {
            case "APPLE" -> handleAppleOauth(request.identifier(), request.name());
            case "GOOGLE" -> handleGoogleOauth(request.identifier(), request.name(), request.department());
            default -> throw new IllegalArgumentException("Unsupported provider: " + request.provider());
        };
    }

    private OauthDecisionResponse handleAppleOauth(String identifier, String name) {
        return userRepository.findByEmailAndStatusAll(identifier)
                .map(user -> {
                    log.info("[USER] 기존 Apple 회원 조회: identifier={}, status={}", identifier, user.getStatus());
                    return new OauthDecisionResponse(
                            user.getStatus().name(),
                            false,
                            user.getStatus() == UserStatus.ACTIVE,
                            identifier
                    );
                })
                .orElseGet(() -> {
                    log.info("[USER] 신규 Apple 회원 생성: identifier={}", identifier);

                    UserEntity newUser = UserEntity.builder()
                            .email(identifier)
                            .name(name != null ? name : identifier)
                            .department(Department.OTHERS)
                            .profileImageUrl(s3Service.getDefaultProfileImageUrl())
                            .status(UserStatus.DISABLED)
                            .role(UserRole.USER)
                            .build();

                    userRepository.save(newUser);

                    log.info("[USER] 신규 Apple 회원 저장 완료: identifier={}, status=DISABLED", identifier);

                    return new OauthDecisionResponse("DISABLED", true, false, identifier);
                });
    }

    private OauthDecisionResponse handleGoogleOauth(String identifier, String name, String department) {
        return userRepository.findByEmailAndStatusAll(identifier)
                .map(user -> {
                    log.info("[USER] 기존 Google 회원 조회: identifier={}, status={}", identifier, user.getStatus());
                    return new OauthDecisionResponse(
                            user.getStatus().name(),
                            false,
                            user.getStatus() == UserStatus.ACTIVE,
                            identifier
                    );
                })
                .orElseGet(() -> {
                    log.info("[USER] 신규 Google 회원 생성: identifier={}", identifier);

                    String deptDesc = (department != null) ? department.replace("/", "").trim() : "";
                    Department dept = Department.fromDescription(deptDesc);

                    UserEntity newUser = UserEntity.builder()
                            .email(identifier)
                            .name(name != null ? name : identifier)
                            .department(dept)
                            .profileImageUrl(s3Service.getDefaultProfileImageUrl())
                            .status(UserStatus.DISABLED)
                            .role(UserRole.USER)
                            .build();

                    userRepository.save(newUser);

                    log.info("[USER] 신규 Google 회원 저장 완료: identifier={}, status=DISABLED", identifier);

                    return new OauthDecisionResponse("DISABLED", true, false, identifier);
                });
    }
}