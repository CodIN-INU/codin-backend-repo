package inu.codin.codin.domain.email.entity;

import inu.codin.codin.common.BaseTimeEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "auth-emails")
@Getter
public class EmailAuthEntity extends BaseTimeEntity {

    @Id @NotBlank
    private String id;

    @NotBlank
    private String email;

    @NotBlank
    private String authNum;

    private boolean isVerified = false;

    private String userId = null;

    @Builder
    public EmailAuthEntity(String email, String authNum, boolean isVerified, String userId) {
        this.email = email;
        this.authNum = authNum;
    }

    public void verifyEmail() {
        this.isVerified = true;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
