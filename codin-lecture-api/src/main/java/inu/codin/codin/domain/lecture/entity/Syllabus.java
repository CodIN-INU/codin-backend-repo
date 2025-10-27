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

    public String getFullText() {
        if (fullText == null) {
            StringBuilder sb = new StringBuilder();

            appendIfNotNull(sb, "개설 대학/대학원명", college);
            appendIfNotNull(sb, "개설 주체 학과(부)", departmentName);
            appendIfNotNull(sb, "이수구분", completionCategory);
            appendIfNotNull(sb, "이수영역", completionArea);
            appendIfNotNull(sb, "학수번호", courseCode);
            appendIfNotNull(sb, "시간표(교시)", timetableText);
            appendIfNotNull(sb, "수업구분", classDivision);
            appendIfNotNull(sb, "수업유형", classTypeText);
            appendIfNotNull(sb, "성적평가", gradingText);
            appendIfNotNull(sb, "원어강의 여부", foreignLanguage != null && foreignLanguage ? "원어강의" : null);

            appendIfNotNull(sb, "총 시수", totalHours);
            appendIfNotNull(sb, "이론 시수", theoryHours);
            appendIfNotNull(sb, "실습 시수", practiceHours);
            appendIfNotNull(sb, "실험 시수", experimentHours);

            appendIfNotNull(sb, "강의 개요", overview);
            appendIfNotNull(sb, "강의 목표", objective);
            appendIfNotNull(sb, "평가 방법", assessmentMethod);

            appendIfNotNull(sb, "1주차", week01);
            appendIfNotNull(sb, "2주차", week02);
            appendIfNotNull(sb, "3주차", week03);
            appendIfNotNull(sb, "4주차", week04);
            appendIfNotNull(sb, "5주차", week05);
            appendIfNotNull(sb, "6주차", week06);
            appendIfNotNull(sb, "7주차", week07);
            appendIfNotNull(sb, "8주차", week08);
            appendIfNotNull(sb, "9주차", week09);
            appendIfNotNull(sb, "10주차", week10);
            appendIfNotNull(sb, "11주차", week11);
            appendIfNotNull(sb, "12주차", week12);
            appendIfNotNull(sb, "13주차", week13);
            appendIfNotNull(sb, "14주차", week14);
            appendIfNotNull(sb, "15주차", week15);
            appendIfNotNull(sb, "16주차", week16);

            appendIfNotNull(sb, "주교재1 제목", mainTextbook1Title);
            appendIfNotNull(sb, "주교재1 저자", mainTextbook1Author);
            appendIfNotNull(sb, "주교재1 출판사", mainTextbook1Publisher);
            appendIfNotNull(sb, "주교재1 출판년도", mainTextbook1Year);

            appendIfNotNull(sb, "참고서적1 제목", refBook1Title);
            appendIfNotNull(sb, "참고서적1 저자", refBook1Author);
            appendIfNotNull(sb, "참고서적1 출판사", refBook1Publisher);
            appendIfNotNull(sb, "참고서적1 출판년도", refBook1Year);

            appendIfNotNull(sb, "주교재2 제목", mainTextbook2Title);
            appendIfNotNull(sb, "주교재2 저자", mainTextbook2Author);
            appendIfNotNull(sb, "주교재2 출판사", mainTextbook2Publisher);
            appendIfNotNull(sb, "주교재2 출판년도", mainTextbook2Year);

            appendIfNotNull(sb, "참고서적2 제목", refBook2Title);
            appendIfNotNull(sb, "참고서적2 저자", refBook2Author);
            appendIfNotNull(sb, "참고서적2 출판사", refBook2Publisher);
            appendIfNotNull(sb, "참고서적2 출판년도", refBook2Year);

            fullText = sb.toString().trim();
        }
        return fullText;
    }

    private void appendIfNotNull(StringBuilder sb, String label, Object value) {
        if (value != null) {
            sb.append(label).append(": ").append(value).append(" ");
        }
    }
}
