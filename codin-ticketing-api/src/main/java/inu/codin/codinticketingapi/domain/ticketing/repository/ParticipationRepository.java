package inu.codin.codinticketingapi.domain.ticketing.repository;

import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryDto;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
            p.status
        )
        FROM Participation p
        JOIN p.event e
        WHERE p.profile.userId = :userId
          AND e.deletedAt IS NULL
          AND p.status = :status
        ORDER BY p.createdAt DESC
        """)
    Page<EventParticipationHistoryDto> findHistoryByUserIdAndCanceled(
            @Param("userId") String userId,
            @Param("status") ParticipationStatus status,
            Pageable pageable
    );

}
