package inu.codin.codin.domain.calendar.repository;

import inu.codin.codin.domain.calendar.entity.CalendarEntity;
import inu.codin.common.entity.Department;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarRepository extends MongoRepository<CalendarEntity, ObjectId> {

    @Query("{ 'endDate': { $gte: ?0 }, 'startDate': { $lte: ?1 }, 'deleted_at': null }")
    List<CalendarEntity> findByEndDateGreaterThanEqualAndStartDateLessThanEqual(LocalDate endDate, LocalDate startDate);

    @Query("{ '_id': ?0, 'deleted_at': null }")
    Optional<CalendarEntity> findByIdAndNotDeleted(ObjectId id);


    @Query("{ 'department': ?0, 'endDate': { $gte: ?1 }, 'startDate': { $lte: ?2 }, 'deleted_at': null }")
    List<CalendarEntity> findDepartmentEventsBetween(
            Department department, LocalDate start, LocalDate end
    );

    @Query("{ 'department': null, 'endDate': { $gte: ?0 }, 'startDate': { $lte: ?1 }, 'deleted_at': null }")
    List<CalendarEntity> findGlobalEventsBetween(
            LocalDate start, LocalDate end
    );
}
