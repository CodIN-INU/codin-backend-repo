package inu.codin.codin.domain.user.feign;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import inu.codin.codin.common.Department;
import inu.codin.codin.common.util.AESUtil;
import inu.codin.codin.domain.user.dto.request.UserSignUpRequestDto;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.feign.dto.UserPortalSignUpRequestDto;
import inu.codin.codin.domain.user.feign.dto.UserPortalSignUpResponseDto;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PortalService {

    private final PortalClient client;
    private final UserRepository userRepository;

    public void signUp(UserSignUpRequestDto userSignUpRequestDto) throws Exception {
        String password = AESUtil.encrypt(userSignUpRequestDto.getPassword());
        String html = client.signUp(UserPortalSignUpRequestDto.builder()
                ._enpass_login_("submit")
                .username(AESUtil.encrypt(userSignUpRequestDto.getStudentId()))
                .password(password)
                .build());
        Document doc = Jsoup.parse(html);

        // todo 가져오는 tag 맞는지 확인
        String value = doc.select(".main").select("input[type=hidden]").attr("value");
        System.out.println(value);

        UserPortalSignUpResponseDto userPortalSignUpResponseDto = readJson(value);
        userPortalSignUpResponseDto.setPassword(password);
        UserEntity userEntity = UserEntity.of(userPortalSignUpResponseDto);
        userRepository.save(userEntity);
    }

    private static UserPortalSignUpResponseDto readJson(String value) throws IOException {
        JsonFactory jsonFactory = JsonFactory.builder().build();
        JsonParser parser = jsonFactory.createParser(value);
        UserPortalSignUpResponseDto userPortalSignUpResponseDto = new UserPortalSignUpResponseDto();
        try {
            // 시작 데이터가 Object인지 확인
            if (parser.currentToken() == JsonToken.START_OBJECT) {

                // Object 데이터 끝이 될때까지 반복합니다.
                while (parser.nextToken() != JsonToken.END_OBJECT) {

                    // JSON의 키 값을 가져옵니다.
                    String fieldName = parser.getCurrentName();
                    parser.nextToken();

                    // JSON의 키 값을 기반으로 값을 추출합니다.
                    if ("studId".equals(fieldName)) {
                        String studId = parser.getValueAsString();
                        userPortalSignUpResponseDto.setStudentId(studId);
                    } else if ("userNm".equals(fieldName)) {
                        String userNm = parser.getValueAsString();
                        userPortalSignUpResponseDto.setName(userNm);
                    } else if ("userEml".equals(fieldName)) {
                        String userEml = parser.getValueAsString();
                        userPortalSignUpResponseDto.setEmail(userEml);

//                    }else if ("userMptel".equals(fieldName)) {//전화번호
//                        String userMptel = parser.getValueAsString();
//                    }else if ("userScregStaNm".equals(fieldName)) { //재학
//                        String userScregStaNm = parser.getValueAsString();
//                    }else if ("schgrNm".equals(fieldName)) { //학년
//                        String schgrNm = parser.getValueAsString();

                    } else if ("userDpmtNm".equals(fieldName)) {
                        String userDpmtNm = parser.getValueAsString();
                        Department department = Department.fromDescription(userDpmtNm);
                        userPortalSignUpResponseDto.setDepartment(department);

//                    }else if ("userCollNm".equals(fieldName)) {
//                        String userCollNm = parser.getValueAsString();
//                    }

                        parser.close();

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userPortalSignUpResponseDto;
    }
}
