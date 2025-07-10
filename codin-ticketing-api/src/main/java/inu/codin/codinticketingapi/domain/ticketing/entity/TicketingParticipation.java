package inu.codin.codinticketingapi.domain.ticketing.entity;

import inu.codin.codinticketingapi.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "ticketing_participation",
        indexes = {
                @Index(name = "idx_event_profile", columnList = "event_id, profile_id", unique = true)
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TicketingParticipation extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 이벤트에 참여했는지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    private TicketingEvent event;

    // 누가 참여했는지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id")
    private TicketingProfile profile;

    // 경품 수령 여부
    @Column(name = "confirmed", nullable = false)
    private boolean confirmed = false;

    // 서명 이미지 URL
    @Column(name = "signature_img_url")
    private String signatureImgUrl;

    @Builder
    public TicketingParticipation(TicketingEvent event, TicketingProfile profile) {
        this.event = event;
        this.profile = profile;
    }

    /** 경품 수령 처리 */
    public void confirm(String signatureImgUrl) {
        this.confirmed = true;
        this.signatureImgUrl = signatureImgUrl;
    }

    /** 수령 상태 초기화 */
    public void reset() {
        this.confirmed = false;
        this.signatureImgUrl = null;
    }
}
