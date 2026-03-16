package inu.codin.codinticketingapi.domain.ticketing.repository;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.admin.entity.EventStatus;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.common.entity.College;
import inu.codin.common.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = "stock")
    @Query("""
            SELECT e
                 FROM Event e
                 WHERE e.deletedAt IS NULL
                    AND e.campus = :campus
                    AND (
                            (e.department IS NOT NULL AND e.department = :department)
                            OR (e.department IS NULL AND e.college = :college)
                    )
                 ORDER BY
                     CASE WHEN e.eventStatus = 'ENDED' THEN 1 ELSE 0 END ASC,
                     CASE WHEN e.eventStatus <> 'ENDED' THEN e.eventEndTime END ASC NULLS LAST,
                     CASE WHEN e.eventStatus = 'ENDED' THEN e.eventEndTime END DESC NULLS LAST
            """)
    Page<Event> findByCampus(@Param("campus") Campus campus,
                             @Param("college") College college,
                             @Param("department") Department department,
                             Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.id = :eventId AND e.deletedAt IS NULL")
    Optional<Event> findById(@Param("eventId") Long eventId);

    @Query("""
    SELECT e
    FROM Event e
    WHERE e.deletedAt IS NULL
    AND (
        :isAdmin = TRUE
        OR (:isManager = TRUE AND e.userId = :userId)
    )
""")
    Page<Event> findAll(
            @Param("userId") String userId,
            @Param("isAdmin") Boolean isAdmin,
            @Param("isManager") Boolean isManager,
            Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.deletedAt IS NULL AND e.userId = :userId")
    Page<Event> findByCreatedUserId(@Param("userId") String userId, Pageable pageable);

    @Query("""
    SELECT e
    FROM Event e
    WHERE e.deletedAt IS NULL
      AND e.eventStatus = :eventStatus
      AND e.eventTime > :eventTimeAfter
      AND (
        :isAdmin = TRUE
        OR (:isManager = TRUE AND e.userId = :userId)
      )
""")
    List<Event> findByEventStatusAndEventTimeAfterAndDeletedAtIsNull(
            @Param("eventStatus") EventStatus eventStatus,
            @Param("userId") String userId,
            @Param("isAdmin") boolean isAdmin,
            @Param("isManager") boolean isManager,
            @Param("eventTimeAfter") LocalDateTime eventTimeAfter);

    @Query("""
    SELECT e
    FROM Event e
    WHERE e.deletedAt IS NULL
      AND e.eventStatus = :eventStatus
      AND (
        :isAdmin = TRUE
        OR (:isManager = TRUE AND e.userId = :userId)
      )
""")
    Page<Event> findAllByEventStatusAndDeletedAtIsNull(
            @Param("eventStatus") EventStatus eventStatus,
            @Param("userId") String userId,
            @Param("isAdmin") boolean isAdmin,
            @Param("isManager") boolean isManager,
            Pageable pageable);

    @Query("""
    SELECT e
    FROM Event e
    WHERE e.deletedAt IS NULL
      AND e.eventStatus = :eventStatus
      AND e.eventEndTime > :now
      AND (
        :isAdmin = TRUE
        OR (:isManager = TRUE AND e.userId = :userId)
      )
""")
    List<Event> findByEventStatusAndEventEndTimeAfterAndDeletedAtIsNull(
            @Param("eventStatus") EventStatus eventStatus,
            @Param("userId") String userId,
            @Param("isAdmin") boolean isAdmin,
            @Param("isManager") boolean isManager,
            @Param("now") LocalDateTime now);

    List<Event> findByEventStatus(EventStatus eventStatus);

    @Query("""
            SELECT e
            FROM Event  e
            WHERE e.eventStatus = :status and :currentTime BETWEEN e.eventTime AND e.eventEndTime
            """)
    List<Event> findAllLiveEvent(@Param("status") EventStatus status, @Param("currentTime") LocalDateTime currentTime);
}
