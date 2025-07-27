package inu.codin.codin.domain.lecture.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class LectureSemester {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id")
    private Semester semester;
}
