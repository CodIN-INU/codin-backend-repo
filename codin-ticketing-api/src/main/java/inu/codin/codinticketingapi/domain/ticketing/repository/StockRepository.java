package inu.codin.codinticketingapi.domain.ticketing.repository;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByEvent(Event event);

    @Query("select s from Stock s where s.event.id = :eventId")
    Optional<Stock> findByEvent_Id(Long eventId);


    @Modifying
    @Query("update Stock s set s.remainingStock = s.remainingStock - 1 where s.event.id = :eventId and s.remainingStock > 0")
    int decrementStockByEventId(Long eventId);
}
