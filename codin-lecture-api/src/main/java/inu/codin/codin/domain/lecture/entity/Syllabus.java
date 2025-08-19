package inu.codin.codin.domain.lecture.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Syllabus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 전체 텍스트 캐시 */
    @Transient
    private String fullText;

    // 기본 메타/분류 정보

    /** 개설 대학/대학원명 (원문 보존) */
    @Column(name = "college")
    private String college;

    /** 개설 주체 학과(부) (원문 보존) */
    @Column(name = "department_name")
    private String departmentName;

    /** 이수구분(전필/전선/교양 등 원문 보존) */
    @Column(name = "completion_category")
    private String completionCategory;

    /** 이수영역 */
    @Column(name = "completion_area")
    private String completionArea;

    /** 학수번호 */
    @Column(name = "course_code")
    private String courseCode;

    /** 시간표(교시) – 예: 월3,수4 같은 원문 문자열 */
    @Column(name = "timetable_text")
    private String timetableText;

    /** 수업구분(일반과목/졸업과제 등) */
    @Column(name = "class_division")
    private String classDivision;

    /** 수업유형(RISE/e-Learning 등) */
    @Column(name = "class_type_text")
    private String classTypeText;

    /** 성적평가(원문 보존: 절대/상대/이수인정 등) */
    @Column(name = "grading_text")
    private String gradingText;

    /** 원어강의 여부 */
    @Column(name = "is_foreign_language")
    private Boolean foreignLanguage;

    // 시수 정보

    /** 총 시수 */
    @Column(name = "total_hours")
    private Integer totalHours;

    /** 이론 시수 */
    @Column(name = "theory_hours")
    private Integer theoryHours;

    /** 실습 시수 */
    @Column(name = "practice_hours")
    private Integer practiceHours;

    /** 실험 시수 */
    @Column(name = "experiment_hours")
    private Integer experimentHours;

    // 강의 개요/목표/평가

    /** 강의 개요 */
    @Lob @Column(name = "overview", columnDefinition = "TEXT")
    private String overview;

    /** 강의 목표 */
    @Lob @Column(name = "objective", columnDefinition = "TEXT")
    private String objective;

    /** 평가 방법(비율·기준 설명 등) */
    @Lob @Column(name = "assessment_method", columnDefinition = "TEXT")
    private String assessmentMethod;

    // 주차별(1~16주) 수업 계획
    @Lob @Column(name = "week01", columnDefinition = "TEXT")
    private String week01;
    @Lob @Column(name = "week02", columnDefinition = "TEXT")
    private String week02;
    @Lob @Column(name = "week03", columnDefinition = "TEXT")
    private String week03;
    @Lob @Column(name = "week04", columnDefinition = "TEXT")
    private String week04;
    @Lob @Column(name = "week05", columnDefinition = "TEXT")
    private String week05;
    @Lob @Column(name = "week06", columnDefinition = "TEXT")
    private String week06;
    @Lob @Column(name = "week07", columnDefinition = "TEXT")
    private String week07;
    @Lob @Column(name = "week08", columnDefinition = "TEXT")
    private String week08;
    @Lob @Column(name = "week09", columnDefinition = "TEXT")
    private String week09;
    @Lob @Column(name = "week10", columnDefinition = "TEXT")
    private String week10;
    @Lob @Column(name = "week11", columnDefinition = "TEXT")
    private String week11;
    @Lob @Column(name = "week12", columnDefinition = "TEXT")
    private String week12;
    @Lob @Column(name = "week13", columnDefinition = "TEXT")
    private String week13;
    @Lob @Column(name = "week14", columnDefinition = "TEXT")
    private String week14;
    @Lob @Column(name = "week15", columnDefinition = "TEXT")
    private String week15;
    @Lob @Column(name = "week16", columnDefinition = "TEXT")
    private String week16;

    // 주교재 1
    @Column(name = "main_textbook1_title")
    private String mainTextbook1Title;
    @Column(name = "main_textbook1_author")
    private String mainTextbook1Author;
    @Column(name = "main_textbook1_publisher")
    private String mainTextbook1Publisher;
    @Column(name = "main_textbook1_year")
    private String mainTextbook1Year;

    // 참고서적 1
    @Column(name = "ref_book1_title")
    private String refBook1Title;
    @Column(name = "ref_book1_author")
    private String refBook1Author;
    @Column(name = "ref_book1_publisher")
    private String refBook1Publisher;
    @Column(name = "ref_book1_year")
    private String refBook1Year;

    // 주교재 2
    @Column(name = "main_textbook2_title")
    private String mainTextbook2Title;
    @Column(name = "main_textbook2_author")
    private String mainTextbook2Author;
    @Column(name = "main_textbook2_publisher")
    private String mainTextbook2Publisher;
    @Column(name = "main_textbook2_year")
    private String mainTextbook2Year;

    // 참고서적 2
    @Column(name = "ref_book2_title")
    private String refBook2Title;
    @Column(name = "ref_book2_author")
    private String refBook2Author;
    @Column(name = "ref_book2_publisher")
    private String refBook2Publisher;
    @Column(name = "ref_book2_year")
    private String refBook2Year;

    /**
     * 강의계획서 전체 내용을 하나의 문자열로 합쳐 반환.
     * null 값은 무시하고 공백으로 구분합니다.
     */
    public String getFullText() {
        if (fullText == null) {
            StringBuilder sb = new StringBuilder();

            appendIfNotNull(sb, college);
            appendIfNotNull(sb, departmentName);
            appendIfNotNull(sb, completionCategory);
            appendIfNotNull(sb, completionArea);
            appendIfNotNull(sb, courseCode);
            appendIfNotNull(sb, timetableText);
            appendIfNotNull(sb, classDivision);
            appendIfNotNull(sb, classTypeText);
            appendIfNotNull(sb, gradingText);
            appendIfNotNull(sb, foreignLanguage != null && foreignLanguage ? "원어강의" : null);

            appendIfNotNull(sb, totalHours);
            appendIfNotNull(sb, theoryHours);
            appendIfNotNull(sb, practiceHours);
            appendIfNotNull(sb, experimentHours);

            appendIfNotNull(sb, overview);
            appendIfNotNull(sb, objective);
            appendIfNotNull(sb, assessmentMethod);

            appendIfNotNull(sb, week01);
            appendIfNotNull(sb, week02);
            appendIfNotNull(sb, week03);
            appendIfNotNull(sb, week04);
            appendIfNotNull(sb, week05);
            appendIfNotNull(sb, week06);
            appendIfNotNull(sb, week07);
            appendIfNotNull(sb, week08);
            appendIfNotNull(sb, week09);
            appendIfNotNull(sb, week10);
            appendIfNotNull(sb, week11);
            appendIfNotNull(sb, week12);
            appendIfNotNull(sb, week13);
            appendIfNotNull(sb, week14);
            appendIfNotNull(sb, week15);
            appendIfNotNull(sb, week16);

            appendIfNotNull(sb, mainTextbook1Title);
            appendIfNotNull(sb, mainTextbook1Author);
            appendIfNotNull(sb, mainTextbook1Publisher);
            appendIfNotNull(sb, mainTextbook1Year);

            appendIfNotNull(sb, refBook1Title);
            appendIfNotNull(sb, refBook1Author);
            appendIfNotNull(sb, refBook1Publisher);
            appendIfNotNull(sb, refBook1Year);

            appendIfNotNull(sb, mainTextbook2Title);
            appendIfNotNull(sb, mainTextbook2Author);
            appendIfNotNull(sb, mainTextbook2Publisher);
            appendIfNotNull(sb, mainTextbook2Year);

            appendIfNotNull(sb, refBook2Title);
            appendIfNotNull(sb, refBook2Author);
            appendIfNotNull(sb, refBook2Publisher);
            appendIfNotNull(sb, refBook2Year);

            fullText = sb.toString().trim();
        }
        return fullText;
    }

    private void appendIfNotNull(StringBuilder sb, Object value) {
        if (value != null) {
            sb.append(value).append(" ");
        }
    }
}
