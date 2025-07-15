package inu.codin.codin.common.security.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.dto.SignUpAndLoginRequestDto;
import inu.codin.codin.common.security.service.oauth2.AbstractAuthService;
import inu.codin.codin.domain.user.dto.request.UserProfileRequestDto;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.exception.UserCreateFailException;
import inu.codin.codin.domain.user.exception.UserNicknameDuplicateException;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AuthCommonService extends AbstractAuthService {


    public AuthCommonService(UserRepository userRepository, S3Service s3Service, JwtService jwtService, UserDetailsService userDetailsService) {
        super(userRepository, s3Service, jwtService, userDetailsService);
    }

    public void completeUserProfile(UserProfileRequestDto userProfileRequestDto, MultipartFile userImage, HttpServletResponse response) {
        Optional<UserEntity> nickNameDuplicate = userRepository.findByNicknameAndDeletedAtIsNull(userProfileRequestDto.getNickname());
        if (nickNameDuplicate.isPresent()){
            throw new UserNicknameDuplicateException("이미 사용중인 닉네임입니다.");
        }
        Optional<UserEntity> userOpt = userRepository.findByEmailAndDisabled(userProfileRequestDto.getEmail());
        if (userOpt.isEmpty()){
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        UserEntity user = userOpt.get();
        log.info("[completeUserProfile] 사용자 조회 성공: {}", userProfileRequestDto.getEmail());

        String imageUrl = null;
        if (userImage != null && !userImage.isEmpty()) {
            log.info("[프로필 설정] 프로필 이미지 업로드 중...");
            imageUrl = s3Service.handleImageUpload(List.of(userImage)).get(0);
            log.info("[프로필 설정] 프로필 이미지 업로드 완료: {}", imageUrl);
        }
        if (imageUrl == null) {
            imageUrl = s3Service.getDefaultProfileImageUrl();
        }

        user.updateNickname(userProfileRequestDto.getNickname());
        user.updateProfileImageUrl(imageUrl);
        user.activation();
        userRepository.save(user);
        log.info("[completeUserProfile] 프로필 설정 완료: {}", user.getEmail());
        issueJwtToken(user.getEmail(), response);
    }

    public LocalDateTime getSuspensionEndDate(OAuth2User oAuth2User){
        String email = (String) oAuth2User.getAttribute("email");
        Optional<UserEntity> optionalUser = userRepository.findByEmailAndStatusAll(email);
        if (optionalUser.isPresent()){
            UserEntity user = optionalUser.get();
            return user.getTotalSuspensionEndDate();
        } else {
            throw new NotFoundException("유저를 찾을 수 없습니다.");
        }
    }

    public void login(SignUpAndLoginRequestDto signUpAndLoginRequestDto, HttpServletResponse response) {
        Optional<UserEntity> user = userRepository.findByEmail(signUpAndLoginRequestDto.getEmail());
        if (user.isPresent()) {
            issueJwtToken(signUpAndLoginRequestDto.getEmail(), response);
        } else {
            throw new UserCreateFailException("아이디 혹은 비밀번호를 잘못 입력하였습니다.");
        }
    }
}
