package inu.codin.codin.domain.user.service;

import inu.codin.codin.domain.email.entity.EmailAuthEntity;
import inu.codin.codin.domain.email.repository.EmailAuthRepository;
import inu.codin.codin.domain.user.dto.UserCreateRequestDto;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.entity.UserStatus;
import inu.codin.codin.domain.user.exception.UserCreateFailException;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final EmailAuthRepository emailAuthRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void createUser(UserCreateRequestDto userCreateRequestDto) {

        String encodedPassword = passwordEncoder.encode(userCreateRequestDto.getPassword());

        validateUserCreateRequest(userCreateRequestDto);
        log.info("[signUpUser] UserCreateRequestDto : {}", userCreateRequestDto);

        // todo : 중복 이메일, 닉네임 체크, 유저 상태, 유저 역할 변경 기능 추가
        UserEntity user = UserEntity.builder()
                .email(userCreateRequestDto.getEmail())
                .password(encodedPassword)
                .studentId(userCreateRequestDto.getStudentId())
                .name(userCreateRequestDto.getName())
                .nickname(userCreateRequestDto.getNickname())
                .profileImageUrl(userCreateRequestDto.getProfileImageUrl())
                .department(userCreateRequestDto.getDepartment())
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
        log.info("[signUpUser] SIGN UP SUCCESS!! : {}", user.getEmail());
    }

    // todo : 정책적으로 보안 위반 사항 확인 -> 에러 메세지를 통해서 유추 금지
    private void validateUserCreateRequest(UserCreateRequestDto userCreateRequestDto) {
        EmailAuthEntity emailAuth = emailAuthRepository.findByEmail(userCreateRequestDto.getEmail()).orElseThrow(() ->
                new UserCreateFailException("이메일 인증을 먼저 진행해주세요."));
        if (!emailAuth.isVerified())
            throw new UserCreateFailException("이메일 인증을 먼저 진행해주세요.");
        if (userRepository.findByEmail(userCreateRequestDto.getEmail()).isPresent())
            throw new UserCreateFailException("이미 존재하는 이메일입니다.");
        if (userRepository.findByStudentId(userCreateRequestDto.getStudentId()).isPresent())
            throw new UserCreateFailException("이미 존재하는 학번입니다.");
    }

}
