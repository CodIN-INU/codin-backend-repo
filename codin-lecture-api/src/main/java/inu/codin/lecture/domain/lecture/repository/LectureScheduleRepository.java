package inu.codin.lecture.domain.lecture.repository;

import inu.codin.lecture.domain.lecture.entity.LectureSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.DayOfWeek;
import java.util.List;

public interface LectureScheduleRepository extends JpaRepository<LectureSchedule, Long> {

    @Query("""
    SELECT DISTINCT s
    FROM LectureSchedule s
    join fetch s.room r
    join fetch s.lecture l
    WHERE r.buildingNum = :building
    AND s.dayOfWeek = :dayOfWeek
    AND (
        :floor = 0
            OR (r.roomNum between (:floor * 100) and (:floor * 100 + 99))
        )
""")
    List<LectureSchedule> findSchedulesForEmptyRoom(
            int building,
            int floor,
            DayOfWeek dayOfWeek
    );
}
