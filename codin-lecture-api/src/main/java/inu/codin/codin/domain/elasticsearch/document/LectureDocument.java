package inu.codin.codin.domain.elasticsearch.document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "lectures")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LectureDocument {

    // todo: 강의계획서, 강의계획서 AI 요약본 ... 필드가 필요함.

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "nori_autocomplete", searchAnalyzer = "nori")
    private String lectureNm;

    @Field(type = FieldType.Integer)
    private Integer grade;

    @Field(type = FieldType.Keyword)
    private String department;

    @Field(type = FieldType.Double)
    private Double starRating;

    @Field(type = FieldType.Long)
    private Long likes;

    @Field(type = FieldType.Long)
    private Long hits;

    @Field(type = FieldType.Keyword)
    private String professor;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Keyword)
    private String lectureType;

    @Field(type = FieldType.Keyword)
    private String evaluation;

    @Field(type = FieldType.Text, analyzer = "nori")
    private List<String> preCourses;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Keyword)
    private List<String> semesters;

    // 강의계획서 전체
//    @Field(type = FieldType.Text,
//            analyzer = "nori",
//            searchAnalyzer = "nori",
//            indexOptions = IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS,
//            termVector = TermVector.WITH_POSITIONS_OFFSETS,
//            fields = {
//                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
//            })
//    private String syllabus;  // 강의계획서 전체

    // AI 요약본
//    @Field(type = FieldType.Text,
//            analyzer = "standard",
//            searchAnalyzer = "standard",
//            indexOptions = IndexOptions.POSITIONS,
//            termVector = TermVector.NO,
//            store = true)          // 자주 조회되는 요약본만 store
//    private String aiSummary;
}
