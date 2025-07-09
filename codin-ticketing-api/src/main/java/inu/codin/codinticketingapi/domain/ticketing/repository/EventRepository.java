package inu.codin.codinticketingapi.domain.ticketing.repository;

import inu.codin.codinticketingapi.domain.ticketing.entity.TicketingEvent;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends MongoRepository<TicketingEvent, ObjectId> {

    @Query("{'deletedAt': null, 'campus': ?0}")
    Page<TicketingEvent> getTicketingEventsByCampus(String campus, Pageable pageable);

    @Query("{'_id': ?0, 'deletedAt': null}")
    Optional<TicketingEvent> findByIdAndNotDeleted(ObjectId eventId);
}
