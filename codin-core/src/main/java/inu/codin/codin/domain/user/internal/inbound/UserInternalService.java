package inu.codin.codin.domain.user.internal.inbound;

import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;

import inu.codin.codin.domain.user.internal.inbound.dto.CompleteProfileRequest;
import inu.codin.codin.domain.user.internal.inbound.dto.AdminLoginMaterialResponse;
import inu.codin.codin.domain.user.internal.inbound.dto.CompleteProfileResponse;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.exception.UserCreateFailException;
import inu.codin.codin.domain.user.exception.UserNicknameDuplicateException;
import inu.codin.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInternalService {
    private final UserRepository userRepository;
    private final S3Service s3Service;

    /**
     * USER 책임: 프로필 완성 (닉네임 중복 체크 + 이미지 업로드 + 활성화)
     */
    public CompleteProfileResponse completeProfile(CompleteProfileRequest req, MultipartFile image) {

        // 닉네임 중복 체크
        boolean duplicated = userRepository.findByNicknameAndDeletedAtIsNull(req.nickname()).isPresent();
        if (duplicated) {
            throw new UserNicknameDuplicateException("이미 사용중인 닉네임입니다.");
        }

        // 비활성 사용자 조회
        UserEntity user = userRepository.findByEmailAndDisabled(req.email())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        log.info("[USER] completeProfile user found: email={}", req.email());

        // 이미지 처리
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            log.info("[USER] profile image upload start: email={}", req.email());
            imageUrl = s3Service.handleImageUpload(java.util.List.of(image)).get(0);
            log.info("[USER] profile image upload done: email={}, url={}", req.email(), imageUrl);
        }
        if (imageUrl == null) {
            imageUrl = s3Service.getDefaultProfileImageUrl();
        }

        // 업데이트 + 활성화 + 저장
        user.updateNickname(req.nickname());
        user.updateProfileImageUrl(imageUrl);
        user.activation();
        userRepository.save(user);

        log.info("[USER] completeProfile done: email={}", user.getEmail());
        return new CompleteProfileResponse(user.getEmail());
    }

    /**
     * USER 책임: 정지 종료일 조회
     */
    public LocalDateTime getSuspensionEndDate(String email) {
        UserEntity user = userRepository.findByEmailAndStatusAll(email)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        return user.getTotalSuspensionEndDate();
    }

    /**
     * USER 책임: 로그인에 필요한 최소 재료 제공 (encodedPassword)
     * - password 검증은 AUTH 책임
     */
    public AdminLoginMaterialResponse getAdminLoginMaterial(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserCreateFailException("아이디 혹은 비밀번호를 잘못 입력하였습니다."));

        if (user.getPassword() == null) {
            log.info("[USER] password is null: email={}", user.getEmail());
            throw new UserCreateFailException("아이디 혹은 비밀번호를 잘못 입력하였습니다.");
        }

        return new AdminLoginMaterialResponse(user.getEmail(), user.getPassword());
    }
}
