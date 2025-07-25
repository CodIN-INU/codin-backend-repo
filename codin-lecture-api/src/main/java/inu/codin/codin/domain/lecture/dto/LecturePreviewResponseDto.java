package inu.codin.codin.domain.lecture.dto;

import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.entity.LectureTag;
import inu.codin.codin.domain.lecture.entity.Type;
import inu.codin.codin.global.common.entity.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Setter
@Getter
public class LecturePreviewResponseDto {

    @Schema(description = "과목코드", example = "IAA6018")
    private String id;

    @Schema(description = "과목명", example = "운영체제")
    private String title;

    @Schema(description = "교수명", example = "홍길동")
    private String professor;

    @Schema(description = "과목 유형", example = "전공핵심")
    private Type type;

    @Schema(description = "학년", example = "2")
    private int grade;

    @Schema(description = "학점", example = "3")
    private int credit;

    @Schema(description = "태그", example = "['")
    private List<String> tags;

    @Schema(description = "유저의 좋아요 여부", example = "true")
    private Boolean liked;

    @Schema(description = "학과", example = "컴퓨터공학부")
    private String department;

    @Schema(description = "좋아요 수", example = "3")
    private int likes;


    @Builder
    public LecturePreviewResponseDto(String id, String title, String professor, Type type, int grade, int credit, List<String> tags, Boolean liked, String department, int likes) {
        this.id = id;
        this.title = title;
        this.professor = professor;
        this.type = type;
        this.grade = grade;
        this.credit = credit;
        this.tags = tags;
        this.liked = liked;
        this.department = department;
        this.likes = likes;
    }

    public static LecturePreviewResponseDto of(Lecture lecture, boolean liked){
        return LecturePreviewResponseDto.builder()
                .id(lecture.getId().toString())
                .title(lecture.getLectureNm())
                .professor(lecture.getProfessor())
                .type(lecture.getType())
                .grade(lecture.getGrade())
                .credit(lecture.getCredit())
                .tags(getTags(lecture.getTags()))
                .liked(liked)
                .department(lecture.getDepartment().getDescription())
                .likes(lecture.getLikes())
                .build();
    }

    public static List<String> getTags(Set<LectureTag> tags) {
        return tags.stream().map(tag -> tag.getTag().getTagName()).toList();
    }
}
