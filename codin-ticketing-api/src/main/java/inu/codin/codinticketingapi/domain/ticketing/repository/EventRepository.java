package inu.codin.codinticketingapi.domain.ticketing.repository;

import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.entity.TicketingEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<TicketingEvent, Long> {

    @Query("SELECT e FROM TicketingEvent e WHERE e.deletedAt IS NULL AND e.campus = :campus")
    Page<TicketingEvent> findByCampus(@Param("campus") Campus campus, Pageable pageable);

    @Query("SELECT e FROM TicketingEvent e WHERE e.id = :eventId AND e.deletedAt IS NULL")
    Optional<TicketingEvent> findById(@Param("eventId") String eventId);

    @Query("SELECT e FROM TicketingEvent e WHERE e.deletedAt IS NULL")
    Page<TicketingEvent> findAll(Pageable pageable);

    @Query("SELECT e FROM TicketingEvent  e WHERE e.deletedAt IS NULL AND e.userId = :userId")
    Page<TicketingEvent> findByUserId(@Param("userId") String userId, Pageable pageable);
}
