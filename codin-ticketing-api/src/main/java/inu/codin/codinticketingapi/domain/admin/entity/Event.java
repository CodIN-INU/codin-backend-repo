package inu.codin.codinticketingapi.domain.admin.entity;

import inu.codin.codinticketingapi.common.entity.BaseEntity;
import inu.codin.codinticketingapi.domain.admin.dto.request.EventUpdateRequest;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
public class Event extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** MongoDB 유저 ObjectId 문자열 */
    @Column(name = "user_id", nullable = false, length = 24)
    private String userId;

    /** 이벤트를 진행하는 캠퍼스 */
    @Enumerated(EnumType.STRING)
    @Column(name = "campus", nullable = false)
    private Campus campus;

    /** 티켓팅 시작 시간 */
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    /** 티켓팅 종료 시간 */
    @Column(name = "event_end_time", nullable = false)
    private LocalDateTime eventEndTime;

    /** 티켓팅 상품 수령 시작 시간 */
    @Column(name = "event_received_start_time", nullable = false)
    private LocalDateTime eventReceivedStartTime;

    /** 티켓팅 상품 수령 종료 시간 */
    @Column(name = "event_received_end_time", nullable = false)
    private LocalDateTime eventReceivedEndTime;

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
    @OneToOne(mappedBy = "event", fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    private Stock stock;

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

    // 이벤트 진행 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus eventStatus;

    @Builder
    public Event(Long id, String userId, Campus campus, LocalDateTime eventTime, LocalDateTime eventEndTime, LocalDateTime eventReceivedStartTime, LocalDateTime eventReceivedEndTime, String eventImageUrl, String title, String locationInfo, String target, String description, String inquiryNumber, String promotionLink, Stock stock) {
        this.id = id;
        this.userId = userId;
        this.campus = campus;
        this.eventTime = eventTime;
        this.eventEndTime = eventEndTime;
        this.eventReceivedStartTime = eventReceivedStartTime;
        this.eventReceivedEndTime = eventReceivedEndTime;
        this.eventImageUrl = eventImageUrl;
        this.title = title;
        this.locationInfo = locationInfo;
        this.target = target;
        this.description = description;
        this.inquiryNumber = inquiryNumber;
        this.promotionLink = promotionLink;
        this.eventPassword = generateEventPassword();
        this.eventStatus = EventStatus.UPCOMING;
        this.stock = stock;
    }

    public void updateFrom(EventUpdateRequest dto) {
        this.campus = dto.getCampus();
        this.eventTime = dto.getEventTime();
        this.eventEndTime = dto.getEventEndTime();
        this.title = dto.getTitle();
        this.locationInfo = dto.getLocationInfo();
        this.target = dto.getTarget();
        this.description = dto.getDescription();
        this.inquiryNumber = dto.getInquiryNumber();
        this.promotionLink = dto.getPromotionLink();

        if (this.stock != null && this.eventStatus == EventStatus.UPCOMING) {
            this.stock.updateStock(dto.getStock());
        }

        if (this.stock != null && this.eventStatus == EventStatus.ACTIVE) {
            this.stock.stockUpdateForEventInProgress(dto.getStock());
        }
    }

    public void updateImageUrl(String newImageUrl) {
        this.eventImageUrl = newImageUrl;
    }

    public void updateStatus(EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }

    public void closeEvent() {
        this.eventStatus = EventStatus.ENDED;
        this.eventEndTime = LocalDateTime.now();
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    /** 이벤트 비밀번호 생성 (무작위 4자리 숫자) */
    private String generateEventPassword() {
        return String.format("%04d", new Random().nextInt(10000));
    }
}
