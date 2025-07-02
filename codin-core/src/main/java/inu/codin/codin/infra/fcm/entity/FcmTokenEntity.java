package inu.codin.codin.infra.fcm.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.infra.fcm.exception.FcmDuplicatedTokenException;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "fcmToken")
@Getter
public class FcmTokenEntity extends BaseTimeEntity {

    @Id
    private ObjectId _id;

    private ObjectId userId;

    private List<String> fcmTokenList;

    private String deviceType;

    @Builder
    public FcmTokenEntity(ObjectId userId, List<String> fcmTokenList, String deviceType) {
        this.userId = userId;
        this.fcmTokenList = fcmTokenList;
        this.deviceType = deviceType;
    }

    /**
     * 유저의 FcmToken을 추가하는 메서드
     */
    public void addFcmToken(@NotBlank String fcmToken) {
        checkDuplicatedFcmToken(fcmToken);
        fcmTokenList.add(fcmToken);
    }

    /**
     * fcmTokenList안에 중복되는지 확인하는 메서드
     */
    private void checkDuplicatedFcmToken(String fcmToken) {
        if (fcmTokenList.contains(fcmToken)) {
            throw new FcmDuplicatedTokenException("이미 등록된 FCM 토큰입니다.");
        }
    }

    public void deleteFcmToken(String fcmToken) {
        fcmTokenList.remove(fcmToken);
    }
}
