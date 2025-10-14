package inu.codin.codinticketingapi.domain.ticketing.entity;

import inu.codin.codinticketingapi.common.entity.BaseEntity;
import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "ticketing_stock",
        indexes = {
                @Index(name = "idx_stock_event", columnList = "event_id", unique = true)
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이벤트 1:1 매핑
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "event_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_stock_event")
    )
    @Setter
    private Event event;

    /**
     * 남은 재고 수
     */
    @Column(name = "stock", nullable = false)
    private int remainingStock;

    // 초기 재고 수
    @Column(name = "initial_stock", nullable = false)
    private int currentTotalStock;

    @Builder
    public Stock(Event event, int initialStock) {
        this.event = event;
        this.remainingStock = initialStock;
        this.currentTotalStock = initialStock;

        if (event != null && event.getStock() != this) {
            event.setStock(this);
        }
    }

    /**
     * 재고 차감 (원자적 경쟁 방지) - 테스트에서 사용중
     */
    public boolean decrease() {
        if (remainingStock <= 0) {
            return false;
        }
        remainingStock--;
        return true;
    }

    /**
     * 재고 증가 - 티켓팅 취소시
     */
    public boolean increase() {
        if (remainingStock >= currentTotalStock) {
            return false;
        }
        remainingStock++;
        return true;
    }

    public void updateStock(int updateStock) {
        this.remainingStock = updateStock;
        this.currentTotalStock = updateStock;
    }

    public void stockUpdateForEventInProgress(int updateStock) {
        if (this.remainingStock > updateStock) {
            throw new TicketingException(TicketingErrorCode.INSUFFICIENT_TOTAL_STOCK);
        }

        int stockDelta = updateStock - this.currentTotalStock;

        this.remainingStock += stockDelta;
        this.currentTotalStock = updateStock;
    }
}
