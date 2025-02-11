package inu.codin.codin.common.security.service;

import inu.codin.codin.common.Department;
import inu.codin.codin.common.security.dto.PortalLoginResponseDto;
import inu.codin.codin.common.security.dto.SignUpAndLoginRequestDto;
import inu.codin.codin.common.security.feign.inu.InuClient;
import inu.codin.codin.common.security.feign.portal.PortalClient;
import inu.codin.codin.common.util.AESUtil;
import inu.codin.codin.domain.user.dto.request.UserNicknameRequestDto;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.exception.UserCreateFailException;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.redis.service.RedisAuthService;
import inu.codin.codin.infra.s3.S3Service;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static feign.Util.ISO_8859_1;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PortalClient portalClient;
    private final InuClient inuClient;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final JwtService jwtService;
    private final RedisAuthService redisAuthService;
    private final PasswordEncoder passwordEncoder;

    public Integer signUp(SignUpAndLoginRequestDto signUpAndLoginRequestDto, HttpServletResponse response){
        try {//학번으로 회원가입 유무 판단
            Optional<UserEntity> user = userRepository.findByStudentId(signUpAndLoginRequestDto.getStudentId());
            if (user.isPresent()) {
                UsernamePasswordAuthenticationToken authenticationToken
                        = new UsernamePasswordAuthenticationToken(signUpAndLoginRequestDto.getStudentId(), signUpAndLoginRequestDto.getPassword());

                Authentication authentication = authenticationManager.authenticate(authenticationToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                jwtService.createToken(response);
                return 0;
            }
            PortalLoginResponseDto userPortalLoginResponseDto
                    = returnPortalInfo(signUpAndLoginRequestDto);
            redisAuthService.saveUserData(signUpAndLoginRequestDto.getStudentId(), userPortalLoginResponseDto);
            return 1;
        } catch (Exception e) {
            log.error("로그인 중 오류 발생", e);
            throw new UserCreateFailException("아이디 혹은 비밀번호를 잘못 입력하였습니다.");
        }

    }

    private PortalLoginResponseDto returnPortalInfo(SignUpAndLoginRequestDto signUpAndLoginRequestDto) throws Exception {
        String html = portalClient.signUp(
                "submit",
                AESUtil.encrypt(signUpAndLoginRequestDto.getStudentId()),
                AESUtil.encrypt(signUpAndLoginRequestDto.getPassword())
        );
        Document doc = Jsoup.parse(html);
        String value = doc.select(".main").select("input[type=hidden]").attr("value");
        PortalLoginResponseDto userPortalLoginResponseDto = readJson(value);
        String password = passwordEncoder.encode(signUpAndLoginRequestDto.getPassword());
        userPortalLoginResponseDto.setPassword(password);
        userPortalLoginResponseDto.setUndergraduate(isUnderGraduate(signUpAndLoginRequestDto));
        return userPortalLoginResponseDto;
    }

    private PortalLoginResponseDto readJson(String value) {
        String[] arrayList = value.substring(1).split(", ");
        PortalLoginResponseDto userPortalLoginResponseDto = new PortalLoginResponseDto();

        Map<String, Consumer<String>> fieldSetters = Map.of(
                "studId", userPortalLoginResponseDto::setStudentId,
                "userNm", userPortalLoginResponseDto::setName,
                "userEml", userPortalLoginResponseDto::setEmail,
                "userDpmtNm", v -> userPortalLoginResponseDto.setDepartment(Department.fromDescription(v)),
                "userCollNm", userPortalLoginResponseDto::setCollege
        );

        for (String user : arrayList) {
            String[] info = user.split("=");
            if (info.length == 2 && fieldSetters.containsKey(info[0])) { // Only process known fields
                fieldSetters.get(info[0]).accept(info[1]);
            }
        }

        return userPortalLoginResponseDto;
    }


    public boolean isUnderGraduate(@Valid SignUpAndLoginRequestDto signUpAndLoginRequestDto){
        String basic = "Basic " + Base64.getEncoder().encodeToString((signUpAndLoginRequestDto.getStudentId() + ":" + signUpAndLoginRequestDto.getPassword()).getBytes(ISO_8859_1));
        Map<String, String> graduate = inuClient.status(basic);
        return graduate.get("undergraduate").equals("true");
    }

    public void createUser(String studentId, UserNicknameRequestDto userNicknameRequestDto, MultipartFile userImage) {

        PortalLoginResponseDto userData = redisAuthService.getUserData(studentId);
        log.info("[createUser] 요청 데이터: {}", studentId);

        String imageUrl = null;
        if (userImage != null) {
            log.info("[회원가입] 프로필 이미지 업로드 중...");
            imageUrl = s3Service.handleImageUpload(List.of(userImage)).get(0);
            log.info("[회원가입] 프로필 이미지 업로드 완료: {}", imageUrl);
        }

        // imageUrl이 null이면 기본 이미지로 설정
        if (imageUrl == null) {
            imageUrl = s3Service.getDefaultProfileImageUrl(); // S3Service에서 기본 이미지 URL 가져오기

        }

        UserEntity user = UserEntity.of(userData);
        user.updateNickname(new UserNicknameRequestDto(userNicknameRequestDto.getNickname()));
        user.updateProfileImageUrl(imageUrl);
        userRepository.save(user);

        log.info("[signUpUser] SIGN UP SUCCESS!! : {}", user.getStudentId());
    }
}
