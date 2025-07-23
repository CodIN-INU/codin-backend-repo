package inu.codin.codin.domain.lecture.entity;

import inu.codin.codin.domain.lecture.converter.TypeConverter;
import inu.codin.codin.global.common.entity.Department;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.index.Indexed;

import java.util.List;

@Getter
@Entity(name = "lectures")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lecture {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Indexed @NotNull
    private String lectureCode;
    private String lectureNm;
    private int grade; //0 : 전학년
    private int credit;
    private String professor;

    @Enumerated(EnumType.STRING)
    private Department department; //OTHERS : 교양

    @Convert(converter = TypeConverter.class)
    private Type type;

//    @Enumerated(EnumType.STRING)
    private String lectureType;
    private String evaluation; //todo enum 관리?
    private String preCourse;
    private double starRating;
    private int likes;
    private int hits;

//    @OneToOne
//    private Emotion emotion; //todo Emotion을 사용할 지에 대한 유무
    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LectureSemester> semester;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LectureTag> tags;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LectureSchedule> schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lectures_room_id")
    private LectureRoom lectureRoom;

//    public void updateReviewRating(double starRating, int participants, Emotion emotion){
//        this.starRating = starRating;
//        this.participants = participants;
//        this.emotion = emotion;
//    }

}
