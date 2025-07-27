package inu.codin.codin.domain.lecture.dto;

import inu.codin.codin.domain.lecture.entity.Emotion;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.entity.LectureSchedule;
import inu.codin.codin.domain.lecture.entity.Type;
import inu.codin.codin.global.common.entity.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.DayOfWeek;
import java.util.List;

@Getter
public class LectureDetailResponseDto extends LecturePreviewResponseDto {

    @Schema(description = "단과대", example = "정보기술대학")
    private String college;

    @Schema(description = "평가 방식", example = "상대평가")
    private String evaluation;

    @Schema(description = "강의 형태", example = "대면강의")
    private String lectureType;

    @Schema(description = "강의 시간표")
    private List<Schedule> schedule;

    @Schema(description = "선수과목")
    private String preCourse;

    @Schema(description = "후기 평점들의 범위마다 100분율 계산", example = "hard : 30, ok : 20, best : 50")
    private EmotionResponseDto emotion;

    private boolean openKeyword;

    public LectureDetailResponseDto(Long id, String title, String professor, Type type, int grade, int credit, List<String> tags, Department department, Department college, String evaluation, String lectureType, List<Schedule> schedule, String preCourse, EmotionResponseDto emotion, boolean openKeyword, int likes) {
        super(id, title, professor, type, grade, credit, tags, null, department.getDescription(), likes);
        this.college = college.getDescription();
        this.evaluation = evaluation;
        this.lectureType = lectureType;
        this.schedule = schedule;
        this.preCourse = preCourse;
        this.emotion = emotion;
        this.openKeyword = openKeyword;
    }

    public static LectureDetailResponseDto of(Lecture lecture, Emotion emotion, boolean openKeyword){
        List<Schedule> schedules = lecture.getSchedule().stream().map(Schedule::of).toList();
        List<String> tags = LecturePreviewResponseDto.getTags(lecture.getTags());
        return new LectureDetailResponseDto(
                lecture.getId(),
                lecture.getLectureNm(),
                lecture.getProfessor(),
                lecture.getType(),
                lecture.getGrade(), //평균 평점
                lecture.getCredit(), //참여 인원 수
                tags,
                lecture.getDepartment(),
                Department.IT_COLLEGE,
                lecture.getEvaluation().getDescription(),
                lecture.getLectureType(),
                schedules,
                lecture.getPreCourse(),
                emotion.changeToPercentage(),
                openKeyword,
                lecture.getLikes()
        );
    }
    

    private record Schedule(DayOfWeek day, String start, String end) {
        private static Schedule of(LectureSchedule schedule){
            return new Schedule(schedule.getDayOfWeek(), schedule.getStart(), schedule.getEnd());
        }
    }
}
