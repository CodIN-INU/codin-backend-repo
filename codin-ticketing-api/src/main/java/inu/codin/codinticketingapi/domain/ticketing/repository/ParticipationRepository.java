package inu.codin.codinticketingapi.domain.ticketing.repository;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryDto;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    @Query("""
        SELECT new inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryDto(
            e.id,
            e.title,
            e.eventImageUrl,
            e.locationInfo,
            e.eventTime,
            e.eventEndTime,
            p.status
        )
        FROM Participation p
        JOIN p.event e
        WHERE p.userId = :userId
          AND e.deletedAt IS NULL
        ORDER BY p.createdAt DESC
        """)
    Page<EventParticipationHistoryDto> findHistoryByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("""
        SELECT new inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryDto(
            e.id,
            e.title,
            e.eventImageUrl,
            e.locationInfo,
            e.eventTime,
            e.eventEndTime,
            p.status
        )
        FROM Participation p
        JOIN p.event e
        WHERE p.userId = :userId
          AND e.deletedAt IS NULL
          AND p.status = :status
        ORDER BY p.createdAt DESC
        """)
    Page<EventParticipationHistoryDto> findHistoryByUserIdAndCanceled(
            @Param("userId") String userId,
            @Param("status") ParticipationStatus status,
            Pageable pageable
    );

    Optional<Participation> findByEventAndUserId(Event event, String userId);

    Page<Participation> findAllByEvent_Id(Long eventId, Pageable pageable);

    @EntityGraph(attributePaths = {"event"})
    List<Participation> findAllByEvent_Id(Long eventId);

    @Query("""
                SELECT p.signatureImgUrl
                FROM Participation p
                WHERE p.event.id = :eventId AND p.userId = :userId
            """)
    Optional<String> findSignatureImgUrlByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") String userId);

    Optional<Participation> findByEvent_IdAndUserId(Long eventId, String profileUserId);

    @Query("""
        SELECT p
        FROM Participation p
        WHERE p.userId = :userId AND p.event = :event
        """)
    Optional<Participation> findByUserIdAndEvent(String userId, Event event);
}
