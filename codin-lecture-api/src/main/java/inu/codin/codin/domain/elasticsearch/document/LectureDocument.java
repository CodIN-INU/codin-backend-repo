package inu.codin.codin.domain.elasticsearch.document;

import inu.codin.codin.domain.elasticsearch.document.dto.EmotionInfo;
import inu.codin.codin.domain.elasticsearch.document.dto.ScheduleInfo;
import inu.codin.codin.domain.elasticsearch.document.dto.SemesterInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.List;

@Document(indexName = "lectures")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LectureDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "nori_autocomplete", searchAnalyzer = "nori")
    private String lectureNm;

    @Field(type = FieldType.Integer)
    private Integer grade;

    @Field(type = FieldType.Integer)
    private Integer credit;

    @Field(type = FieldType.Keyword)
    private String professor;

    @Field(type = FieldType.Keyword)
    private String department;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Keyword)
    private String lectureType;

    @Field(type = FieldType.Keyword)
    private String evaluation;

    @Field(type = FieldType.Text, analyzer = "nori")
    private List<String> preCourses;

    @Field(type = FieldType.Double)
    private Double starRating;

    @Field(type = FieldType.Long)
    private Integer likes;

    @Field(type = FieldType.Long)
    private Integer hits;

    @Field(type = FieldType.Nested)
    private List<SemesterInfo> semesters;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Nested)
    private List<ScheduleInfo> schedule;

    @Field(type = FieldType.Object)
    private EmotionInfo emotion;

    // 강의계획서 전체
    @MultiField(
            mainField = @Field(type = FieldType.Text,
                    analyzer = "nori",
                    searchAnalyzer = "nori",
                    indexOptions = IndexOptions.offsets,
                    termVector = TermVector.with_positions_offsets),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String syllabus;

    //     AI 요약본
    @Field(type = FieldType.Text,
            analyzer = "standard",
            searchAnalyzer = "standard",
            indexOptions = IndexOptions.positions,
            termVector = TermVector.no,
            store = true)
    private String aiSummary;
}
