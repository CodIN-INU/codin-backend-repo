package inu.codin.codin.domain.lecture.repository;

import inu.codin.codin.domain.lecture.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SemesterRepository extends JpaRepository<Semester, Long> {

    Optional<Semester> findSemesterByYearAndQuarter(Integer year, Integer quarter);
}
