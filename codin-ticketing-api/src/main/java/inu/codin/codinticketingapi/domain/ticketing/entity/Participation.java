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
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ParticipationStatus status = ParticipationStatus.WAITING;

    /** 교환권 번호 */
    @Column(name = "ticket_number", nullable = false)
    private Integer ticketNumber;

    @Setter
    @Column(name = "signature_img_url")
    private String signatureImgUrl;

    @Builder
    public Participation(Event event, Profile profile, Integer ticketNumber) {
        this.event = event;
        this.profile = profile;
        this.ticketNumber = ticketNumber;
    }

    /** 경품 수령 처리 */
    public void changeStatusCompleted() {
        this.status = ParticipationStatus.COMPLETED;
    }

    /** 취소 처리 */
    public void changeStatusCanceled() {
        if (this.status == ParticipationStatus.WAITING) {
            this.status = ParticipationStatus.CANCELED;
        }
    }

    /** 수령 상태 초기화 */
    public void reset() {
        this.status = ParticipationStatus.WAITING;
        this.signatureImgUrl = null;
    }
}
