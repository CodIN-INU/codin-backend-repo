package inu.codin.codin.domain.calendar.repository;

import inu.codin.codin.domain.calendar.entity.CalendarEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalendarRepository extends MongoRepository<CalendarEntity, ObjectId> {

    List<CalendarEntity> findByEndDateGreaterThanEqualAndStartDateLessThanEqual(LocalDate endDate, LocalDate startDate);
}
