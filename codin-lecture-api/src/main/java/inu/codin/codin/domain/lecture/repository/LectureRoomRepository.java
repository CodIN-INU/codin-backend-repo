package inu.codin.codin.domain.lecture.repository;

import inu.codin.codin.domain.lecture.entity.LectureRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureRoomRepository extends JpaRepository<LectureRoom, Long> {

    @Query("SELECT lr FROM LectureRoom lr LEFT JOIN FETCH lr.schedules")
    List<LectureRoom> findAllWithSchedules();
}
