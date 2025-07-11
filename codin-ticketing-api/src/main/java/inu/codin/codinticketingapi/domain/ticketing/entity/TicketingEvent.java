package inu.codin.codinticketingapi.domain.ticketing.entity;

import inu.codin.codinticketingapi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Random;

@Entity
@Table(
        name = "ticketing_event",
        indexes = {
                @Index(name = "idx_user_id",           columnList = "user_id"),
                @Index(name = "idx_campus",            columnList = "campus"),
                @Index(name = "idx_event_time",        columnList = "event_time"),
                @Index(name = "idx_campus_deleted_created", columnList = "campus, deleted_at, created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TicketingEvent extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** MongoDB 유저 ObjectId 문자열 */
    @Column(name = "user_id", nullable = false, length = 24)
    private String userId;

    /** 이벤트를 진행하는 캠퍼스 */
    @Enumerated(EnumType.STRING)
    @Column(name = "campus", nullable = false)
    private Campus campus;

    /** 이벤트 시작 시간 */
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    /** 이벤트 종료 시간 */
    @Column(name = "event_end_time", nullable = false)
    private LocalDateTime eventEndTime;

    /** 이제 단일 이미지 URL */
    @Column(name = "event_image_url")
    private String eventImageUrl;

    /** 이벤트 제목 */
    @Column(name = "title", nullable = false)
    private String title;

    /** 이벤트 위치 (정보대 학생회실) */
    @Column(name = "location_info")
    private String locationInfo;

    /** 제공 수량 (햄버거 100개) */
    @Column(name = "quantity", nullable = false)
    private int quantity;

    /** 이벤트 대상자 (정보대 학생) */
    @Column(name = "target")
    private String target;

    /** 이벤트 설명 (싸이버거 제공) */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 문의 번호 */
    @Column(name = "inquiry_number")
    private String inquiryNumber;

    /** 홍보글 링크 */
    @Column(name = "promotion_link")
    private String promotionLink;

    /** 이벤트 수령확인 관리자 비밀번호 */
    @Column(name = "event_password", nullable = false)
    private String eventPassword;

    @Builder
    public TicketingEvent(String userId, Campus campus, LocalDateTime eventTime, LocalDateTime eventEndTime, String eventImageUrl, String title, String locationInfo, int quantity, String target, String description, String inquiryNumber, String promotionLink) {
        this.userId = userId;
        this.campus = campus;
        this.eventTime = eventTime;
        this.eventEndTime = eventEndTime;
        this.eventImageUrl = eventImageUrl;
        this.title = title;
        this.locationInfo = locationInfo;
        this.quantity = quantity;
        this.target = target;
        this.description = description;
        this.inquiryNumber = inquiryNumber;
        this.promotionLink = promotionLink;
        this.eventPassword = generateEventPassword();
    }

    /** 이벤트 비밀번호 생성 (무작위 4자리 숫자) */
    private String generateEventPassword() {
        return String.format("%04d", new Random().nextInt(10000));
    }
}
