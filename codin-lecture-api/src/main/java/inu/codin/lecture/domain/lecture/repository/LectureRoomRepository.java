package inu.codin.lecture.domain.lecture.repository;

import inu.codin.lecture.domain.lecture.entity.LectureRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureRoomRepository extends JpaRepository<LectureRoom, Long> {

    @Query("""
    SELECT DISTINCT lr
    FROM LectureRoom lr
    LEFT JOIN FETCH lr.schedules s
    LEFT JOIN FETCH s.lecture l
""")
    List<LectureRoom> findAllWithSchedulesAndLectures();

    @Query("""
        SELECT lr
        FROM LectureRoom lr
        WHERE lr.buildingNum = :building
          AND (:floor = 0 OR (lr.roomNum BETWEEN (:floor * 100) AND (:floor * 100 + 99)))
        ORDER BY lr.roomNum
    """)
    List<LectureRoom> findRoomsByBuildingAndFloor(int building, int floor);
}
