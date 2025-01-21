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
import inu.codin.codin.infra.s3.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static feign.Util.ISO_8859_1;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final PortalClient portalClient;
    private final InuClient inuClient;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;

    public void signUp(SignUpAndLoginRequestDto signUpAndLoginRequestDto) throws Exception {
        //학번으로 회원가입 유무 판단
        Optional<UserEntity> user = userRepository.findByStudentId(signUpAndLoginRequestDto.getStudentId());
        if (user.isPresent()) throw new UserCreateFailException("이미 회원가입된 유저입니다.");

        PortalLoginResponseDto userPortalLoginResponseDto
                        = returnPortalInfo(signUpAndLoginRequestDto);
        UserEntity userEntity = UserEntity.of(userPortalLoginResponseDto);
        userRepository.save(userEntity);
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

        for (String user : arrayList) {
            String[] info = user.split("=");
            String fieldName = info[0];

            if ("studId".equals(fieldName)) {
                String studId = info[1];
                userPortalLoginResponseDto.setStudentId(studId);
            } else if ("userNm".equals(fieldName)) {
                String userNm = info[1];
                userPortalLoginResponseDto.setName(userNm);
            } else if ("userEml".equals(fieldName)) {
                String userEml = info[1];
                userPortalLoginResponseDto.setEmail(userEml);
            } else if ("userDpmtNm".equals(fieldName)) {
                String userDpmtNm = info[1];
                Department department = Department.fromDescription(userDpmtNm);
                userPortalLoginResponseDto.setDepartment(department);
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

        UserEntity user = userRepository.findByStudentId(studentId)
                        .orElseThrow(() -> new UserCreateFailException("존재하지 않는 학번입니다. 포탈 로그인부터 진행해주세요."));
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

        user.updateNickname(new UserNicknameRequestDto(userNicknameRequestDto.getNickname()));
        user.updateProfileImageUrl(imageUrl);
        userRepository.save(user);

        log.info("[signUpUser] SIGN UP SUCCESS!! : {}", user.getStudentId());
    }
}
