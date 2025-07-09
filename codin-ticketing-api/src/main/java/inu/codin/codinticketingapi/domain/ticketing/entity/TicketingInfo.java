package inu.codin.codinticketingapi.domain.ticketing.entity;

import inu.codin.codinticketingapi.common.BaseTimeEntity;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Document(collection = "ticketing-infos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TicketingInfo extends BaseTimeEntity {

    @MongoId
    private ObjectId _id;

    @Indexed
    private ObjectId eventId;

    private String eventTitle;
    private List<ProfileInfo> profileInfoList = new ArrayList<>();
    private int confirmedCount;

    @Builder
    public TicketingInfo(ObjectId eventId, String eventTitle, List<ProfileInfo> profileInfoList) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        if (profileInfoList != null) {
            this.profileInfoList = profileInfoList;
        }
        this.confirmedCount = (int) profileInfoList.stream().filter(ProfileInfo::isConfirmed).count();
    }

    // 참여자 추가
    public void addProfile(ObjectId profileId) {
        this.profileInfoList.add(new ProfileInfo(profileId, false, null));
    }

    // 참여자 삭제
    public void deleteProfile(ObjectId profileId) {
        this.profileInfoList.removeIf(info -> info.profileId.equals(profileId));
    }

    // 관리자 확인(서명 이미지와 함께 수령 처리)
    public boolean confirmProfileWithSignature(ObjectId profileId, String inputPassword, String eventPassword, String signatureImgUrl) {
        if (!eventPassword.equals(inputPassword)) {
            return false;
        }
        Optional<ProfileInfo> profileOpt = this.profileInfoList.stream()
                .filter(info -> info.profileId.equals(profileId))
                .findFirst();
        profileOpt.ifPresent(info -> {
            info.confirm(signatureImgUrl);
            this.confirmedCount++;
        });
        return profileOpt.isPresent();
    }

    // 참여자 수령 여부 조회
    public boolean isConfirmed(ObjectId profileId) {
        return this.profileInfoList.stream()
                .filter(info -> info.profileId.equals(profileId))
                .map(ProfileInfo::isConfirmed)
                .findFirst()
                .orElse(false);
    }

    // 수령 확인된 참여자 리스트 반환
    public List<ProfileInfo> getConfirmedProfiles() {
        return this.profileInfoList.stream()
                .filter(ProfileInfo::isConfirmed)
                .toList();
    }

    // 수령하지 않은 참여자 리스트 반환
    public List<ProfileInfo> getNotConfirmedProfiles() {
        return this.profileInfoList.stream()
                .filter(info -> !info.isConfirmed())
                .toList();
    }

    // 모든 참여자의 수령 상태 초기화
    public void resetConfirmation() {
        this.profileInfoList.forEach(info -> {
            info.setConfirmed(false);
            info.setSignatureImgUrl(null);
        });
        this.confirmedCount = 0;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ProfileInfo {
        private ObjectId profileId;
        @Setter
        private boolean confirmed;
        @Setter
        private String signatureImgUrl;

        public ProfileInfo(ObjectId profileId, boolean confirmed, String signatureImgUrl) {
            this.profileId = profileId;
            this.confirmed = confirmed;
            this.signatureImgUrl = signatureImgUrl;
        }

        public void confirm(String signatureImgUrl) {
            this.confirmed = true;
            this.signatureImgUrl = signatureImgUrl;
        }
    }
}