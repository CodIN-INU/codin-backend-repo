package inu.codin.codin.domain.admin.service;

import inu.codin.codin.domain.admin.dto.req.SetManagerUserInfo;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserStatus;
import inu.codin.codin.domain.user.exception.UserEmailDuplicateException;
import inu.codin.codin.domain.user.exception.UserNicknameDuplicateException;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.security.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void addManager(SetManagerUserInfo setManagerUserInfo) {
        boolean emailExists = userRepository.findByEmailAndDeletedAtIsNull(setManagerUserInfo.email()).isPresent();
        if (emailExists) {
            throw new UserEmailDuplicateException("이미 존재하는 이메일입니다.");
        }
        boolean nicknameExists = userRepository.findByNicknameAndDeletedAtIsNull(setManagerUserInfo.nickname()).isPresent();
        if (nicknameExists) {
            throw new UserNicknameDuplicateException("이미 사용중인 닉네임입니다.");
        }

        UserEntity newUser = UserEntity.builder()
                .email(setManagerUserInfo.email())
                .password(passwordEncoder.encode(setManagerUserInfo.password()))
                .studentId(setManagerUserInfo.studentId())
                .name(setManagerUserInfo.name())
                .nickname(setManagerUserInfo.nickname())
                .profileImageUrl(setManagerUserInfo.profileImageUrl())
                .department(setManagerUserInfo.department())
                .college(setManagerUserInfo.college())
                .role(UserRole.MANAGER)
                .status(UserStatus.ACTIVE)
                .build();

        UserEntity savedUser = userRepository.save(newUser);

        log.info("[ADMIN] 신규 MANAGER 회원 저장 완료 id={}, email={}", savedUser.get_id(), savedUser.getEmail());
    }
}