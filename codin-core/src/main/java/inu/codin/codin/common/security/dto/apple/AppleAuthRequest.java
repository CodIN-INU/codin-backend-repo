package inu.codin.codin.common.security.dto.apple;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class AppleAuthRequest {
    private final String identityToken;  // Apple ID Token (JWT)
    private final Map<String, Object> user;  // 최초 로그인 시 제공되는 사용자 정보

    public String getEmail() {
        return (String) user.get("email");
    }

    public String getFullName() {
        Map<String, String> name = (Map<String, String>) user.get("name");
        return name != null ? name.get("lastName") + name.get("firstName") : null;
    }


}