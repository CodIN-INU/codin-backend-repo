package inu.codin.codinticketingapi.domain.ticketing.repository;

import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.admin.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.deletedAt IS NULL AND e.campus = :campus")
    Page<Event> findByCampus(@Param("campus") Campus campus, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.id = :eventId AND e.deletedAt IS NULL")
    Optional<Event> findById(@Param("eventId") Long eventId);

    @Query("SELECT e FROM Event e WHERE e.deletedAt IS NULL")
    Page<Event> findAll(Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.deletedAt IS NULL AND e.userId = :userId")
    Page<Event> findByCreatedUserId(@Param("userId") String userId, Pageable pageable);

}
