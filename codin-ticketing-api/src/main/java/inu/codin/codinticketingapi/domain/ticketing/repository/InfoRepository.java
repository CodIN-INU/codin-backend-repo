package inu.codin.codinticketingapi.domain.ticketing.repository;

import inu.codin.codinticketingapi.domain.ticketing.entity.TicketingParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InfoRepository extends JpaRepository<TicketingParticipation, Long> {

    @Query("SELECT t FROM TicketingParticipation t WHERE t.event = :eventId")
    Optional<TicketingParticipation> findByEventId(@Param("eventId") Long eventId);

    boolean existsByEventId(Long eventId);
}
