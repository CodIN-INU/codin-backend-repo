package inu.codin.codin.domain.elastic.document;

import lombok.*;
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
    // todo: 한글용 Nori 플러그인 기반으로 analyzer 적용

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String lectureNm;

    @Field(type = FieldType.Keyword)
    private String professor;

    @Field(type = FieldType.Keyword)
    private String department;

    @Field(type = FieldType.Double)
    private Double starRating;

    @Field(type = FieldType.Long)
    private Long likes;

    @Field(type = FieldType.Long)
    private Long hits;

    @Field(type = FieldType.Integer)
    private Integer grade;

    @Field(type = FieldType.Keyword)
    private List<String> semesters;
}
