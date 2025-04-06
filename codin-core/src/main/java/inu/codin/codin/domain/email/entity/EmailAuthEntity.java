package inu.codin.codin.domain.email.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "auth-emails")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailAuthEntity extends BaseTimeEntity {

    @Id @NotNull
    private ObjectId _id;

    @NotBlank
    private String email;

    @NotBlank
    private String authNum;

    private boolean isVerified = false;

    @Builder
    public EmailAuthEntity(String email, String authNum) {
        this.email = email;
        this.authNum = authNum;
    }

    public void changeAuthNum(String authNum) {
        this.authNum = authNum;
    }

    public void verifyEmail() {
        this.isVerified = true;
    }

    public void unVerifyEmail(){
        this.isVerified=false;
    }

    /**
     * 10분 이상이면 만료됌
     */
    public boolean isExpired() {
        return getUpdatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }
}
