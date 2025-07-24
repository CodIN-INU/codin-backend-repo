package inu.codin.codinticketingapi.domain.ticketing.entity;

import inu.codin.codinticketingapi.common.entity.BaseEntity;
import inu.codin.codinticketingapi.domain.admin.entity.Event;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Event event;

    /** 남은 재고 수 */
    @Column(name = "stock", nullable = false)
    private int stock;

    // 초기 재고 수
    @Column(name = "initial_stock", nullable = false)
    private int initialStock;

    @Builder
    public Stock(Event event, int initialStock) {
        this.event = event;
        this.stock = initialStock;
        this.initialStock = initialStock;

        if (event != null && event.getStock() != this) {
            event.setStock(this);
        }
    }

    /** 재고 차감 (원자적 경쟁 방지) */
    public boolean decrease() {
        if (stock <= 0) {
            return false;
        }
        stock--;
        return true;
    }

    /** 재고 증가 - 티켓팅 취소시 */
    public boolean increase() {
        if (stock >= initialStock) {
            return false;
        }
        stock++;
        return true;
    }

    public void updateStock(int stock) {
        this.stock = stock;
    }
}
