package inu.codin.codinticketingapi.domain.ticketing.entity;

import inu.codin.codinticketingapi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "ticketing_profile",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id", unique = true)
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TicketingProfile extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** MongoDB ObjectId 문자열 */
    @Column(name = "user_id", nullable = false, length = 24)
    private String userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "department", nullable = false)
    private Department department;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Builder
    public TicketingProfile(String userId, String name, Department department, String studentId) {
        this.userId = userId;
        this.name = name;
        this.department = department;
        this.studentId = studentId;
    }

    public void updateProfile(Department department, String studentId) {
        this.department = department;
        this.studentId = studentId;
    }
}
