package inu.codin.codinticketingapi.domain.ticketing.entity;

import inu.codin.codinticketingapi.common.entity.BaseEntity;
import inu.codin.codinticketingapi.domain.admin.entity.Event;
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
public class Participation extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = @ForeignKey(name = "fk_participation_event"))
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_participation_profile"))
    private Profile profile;

    /** 경품 수령 여부 */
    @Column(name = "confirmed", nullable = false)
    private boolean confirmed = false;

    /** 취소 여부 */
    @Column(name = "canceled", nullable = false)
    private boolean canceled = false;

    /** 교환권 번호 */
    @Column(name = "ticket_number", nullable = false)
    private Integer ticketNumber;

    /** 서명 이미지 URL */
    @Column(name = "signature_img_url")
    private String signatureImgUrl;

    @Builder
    public Participation(Event event, Profile profile, boolean confirmed, boolean canceled, Integer ticketNumber, String signatureImgUrl) {
        this.event = event;
        this.profile = profile;
        this.confirmed = confirmed;
        this.canceled = canceled;
        this.ticketNumber = ticketNumber;
        this.signatureImgUrl = signatureImgUrl;
    }

    /** 경품 수령 처리 */
    public void confirm(String signatureImgUrl) {
        this.confirmed = true;
        this.signatureImgUrl = signatureImgUrl;
    }

    public void changeConfirmStatus() {
        this.confirmed = !confirmed;
    }

    /** 수령 상태 초기화 */
    public void reset() {
        this.confirmed = false;
        this.signatureImgUrl = null;
    }
}
