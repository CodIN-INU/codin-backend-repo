package inu.codin.codin.domain.elasticsearch.document;

import co.elastic.clients.elasticsearch._types.analysis.NoriDecompoundMode;
import co.elastic.clients.elasticsearch._types.mapping.IndexOptions;
import co.elastic.clients.elasticsearch._types.mapping.TermVectorOption;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;

public class LectureIndexMapping {
    public static final String INDEX_NAME = "lectures";

    public static CreateIndexRequest createLectureIndexRequest() {
        IndexSettings settings = createIndexSettingsBuilder();

        return new CreateIndexRequest.Builder()
                .index(INDEX_NAME)
                .settings(settings)
                .mappings(m -> m
                        .properties("id", p -> p
                                .long_(l -> l))
                        .properties("lectureName", p -> p
                                .text(text -> text
                                        .analyzer("nori_autocomplete")
                                        .searchAnalyzer("nori")
                                )
                        )
                        .properties("grade", p -> p
                                .integer(i -> i)
                        )
                        .properties("credit", p -> p
                                .integer(i -> i)
                        )
                        .properties("professor", p -> p
                                .keyword(k -> k)
                        )
                        .properties("department", p -> p
                                .keyword(k -> k)
                        )
                        .properties("type", p -> p
                                .keyword(k -> k)
                        )
                        .properties("lectureType", p -> p
                                .keyword(k -> k)
                        )
                        .properties("evaluation", p -> p
                                .keyword(k -> k)
                        )
                        .properties("preCourses", p -> p
                                .text(text -> text
                                        .analyzer("nori")
                                )
                        )
                        .properties("starRating", p -> p
                                .double_(d -> d)
                        )
                        .properties("likes", p -> p
                                .integer(i -> i)
                        )
                        .properties("hits", p -> p
                                .integer(i -> i)
                        )
                        .properties("semesters", p -> p
                                .nested(n -> n
                                        .properties("year", pp -> pp.integer(i -> i))
                                        .properties("quarter", pp -> pp.integer(i -> i))
                                )
                        )
                        .properties("tags", p -> p
                                .keyword(k -> k)
                        )
                        .properties("schedule", p -> p
                                .nested(n -> n
                                        .properties("day", pp -> pp.keyword(k -> k))
                                        .properties("startTime", pp -> pp.keyword(k -> k))
                                        .properties("endTime", pp -> pp.keyword(k -> k))
                                        .properties("roomInfo", pp -> pp.keyword(k -> k))
                                )
                        )
                        .properties("emotion", p -> p
                                .object(object -> object
                                        .properties("hard", pp -> pp.integer(i -> i))
                                        .properties("ok", pp -> pp.integer(i -> i))
                                        .properties("best", pp -> pp.integer(i -> i))
                                )
                        )
                        .properties("syllabus", p -> p
                                .text(t -> t
                                        .analyzer("nori")
                                        .searchAnalyzer("nori")
                                        .indexOptions(IndexOptions.Offsets)
                                        .termVector(TermVectorOption.WithPositionsOffsets)
                                        .fields("keyword", f -> f
                                                .keyword(k -> k)
                                        )
                                )
                        )
                        .properties("aiSummary", p -> p
                                .text(t -> t
                                        .analyzer("standard")
                                        .searchAnalyzer("standard")
                                        .indexOptions(IndexOptions.Positions)
                                        .termVector(TermVectorOption.No)
                                        .store(true)
                                )
                        )
                )
                .build();
    }

    private static IndexSettings createIndexSettingsBuilder() {
        return new IndexSettings.Builder()
                .analysis(a -> a
                        .tokenizer("nori_tokenizer", type -> type
                                .definition(defin -> defin
                                        .noriTokenizer(nori -> nori
                                                .decompoundMode(NoriDecompoundMode.Mixed)
                                        ))
                        )
                        .filter("autocomplete_filter", filter -> filter
                                .definition(defin -> defin
                                        .edgeNgram(edge -> edge
                                                .minGram(1).maxGram(20)
                                        )
                                )
                        )
                        .analyzer("nori", an -> an
                                .custom(custom -> custom
                                        .tokenizer("nori_tokenizer")
                                        .filter("nori_readingform", "lowercase")
                                )
                        )
                        .analyzer("nori_autocomplete", an -> an
                                .custom(custom -> custom
                                        .tokenizer("nori_tokenizer")
                                        .filter("nori_readingform", "lowercase", "autocomplete_filter")
                                )
                        )
                ).build();
    }
}
