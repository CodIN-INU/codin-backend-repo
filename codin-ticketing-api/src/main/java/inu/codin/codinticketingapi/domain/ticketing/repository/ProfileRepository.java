package inu.codin.codinticketingapi.domain.ticketing.repository;

import inu.codin.codinticketingapi.domain.ticketing.entity.TicketingProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<TicketingProfile, Long> {

    @Query("SELECT p FROM TicketingProfile p WHERE p.userId = :userId")
    Optional<TicketingProfile> findByUserId(@Param("userId") String userId);

    boolean existsByUserId(String userId);
}
