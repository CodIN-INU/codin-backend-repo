package inu.codin.codinticketingapi.domain.ticketing.repository;

import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryDto;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
                p.confirmed
            )
            FROM Participation p
            JOIN p.event e
            WHERE p.profile.userId = :userId
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
                p.confirmed
            )
            FROM Participation p
            JOIN p.event e
            WHERE p.profile.userId = :userId
              AND e.deletedAt IS NULL
              AND p.canceled = :canceled
            ORDER BY p.createdAt DESC
            """)
    Page<EventParticipationHistoryDto> findHistoryByUserIdAndCanceled(
            @Param("userId") String userId,
            @Param("canceled") boolean canceled,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"profile"})
    Page<Participation> findAllByEvent_Id(Long eventId, Pageable pageable);

    @Query("""
                SELECT p.signatureImgUrl
                FROM Participation p
                WHERE p.event.id = :eventId AND p.profile.userId = :userId
            """)
    Optional<String> findSignatureImgUrlByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") String userId);

    Optional<Participation> findByEvent_IdAndProfile_UserId(Long eventId, String profileUserId);
}
