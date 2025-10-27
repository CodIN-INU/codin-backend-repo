package inu.codin.codinticketingapi.domain.ticketing.entity;

import inu.codin.codinticketingapi.common.entity.BaseEntity;
import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.user.dto.UserInfoResponse;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "ticketing_participation",
        indexes = {
                @Index(name = "idx_event_profile", columnList = "event_id, user_id", unique = true)
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

    @Column(name = "user_id", nullable = false, length = 24)
    private String userId;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "department", nullable = false)
    private Department department;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    /** 경품 수령 여부 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ParticipationStatus status = ParticipationStatus.WAITING;

    /** 교환권 번호 */
    @Column(name = "ticket_number", nullable = false)
    private Integer ticketNumber;

    @Column(name = "signature_img_url")
    private String signatureImgUrl;

    @Builder
    public Participation(Event event, Integer ticketNumber, UserInfoResponse userInfoResponse) {
        this.event = event;
        this.ticketNumber = ticketNumber;
        this.userId = userInfoResponse.getUserId();
        this.name = userInfoResponse.getName();
        this.studentId = userInfoResponse.getStudentId();
        this.department = userInfoResponse.getDepartment();
    }

    /** 경품 수령 처리 */
    public void changeStatusCompleted(String imageUrl) {
        this.status = ParticipationStatus.COMPLETED;
        this.signatureImgUrl = imageUrl;
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
