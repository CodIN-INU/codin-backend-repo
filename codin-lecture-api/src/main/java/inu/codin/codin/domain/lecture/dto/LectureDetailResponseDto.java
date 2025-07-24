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

    @Schema(description = "학부", example = "컴퓨터공학부")
    private String department;

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
    private Emotion emotion;

    public LectureDetailResponseDto(String id, String title, String professor, Type type, int grade, int credit, List<String> tags, Department department, Department college, String evaluation, String lectureType, List<Schedule> schedule, String preCourse, Emotion emotion) {
        super(id, title, professor, type, grade, credit, tags, null);
        this.department = department.getDescription();
        this.college = college.getDescription();
        this.evaluation = evaluation;
        this.lectureType = lectureType;
        this.schedule = schedule;
        this.preCourse = preCourse;
        this.emotion = emotion;
    }

    public static LectureDetailResponseDto of(Lecture lecture){
        List<Schedule> schedules = lecture.getSchedule().stream().map(Schedule::of).toList();
        List<String> tags = LecturePreviewResponseDto.getTags(lecture.getTags());
        return new LectureDetailResponseDto(
                lecture.getId().toString(),
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
                lecture.getEmotion()
        );
    }
    

    private record Schedule(DayOfWeek day, String start, String end) {
        private static Schedule of(LectureSchedule schedule){
            return new Schedule(schedule.getDayOfWeek(), schedule.getStart(), schedule.getEnd());
        }
    }
}
