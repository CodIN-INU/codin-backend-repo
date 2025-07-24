package inu.codin.codin.domain.elastic.service;


import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import inu.codin.codin.domain.elastic.document.LectureDocument;
import inu.codin.codin.domain.lecture.entity.SortingOption;
import inu.codin.codin.global.common.entity.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class LectureElasticService {

    private final ElasticsearchOperations elasticsearchOperations;

    public SearchPage<LectureDocument> search(
            String keyword,
            Department department,
            SortingOption sortOption,
            int pageNumber,
            int size
    ) {

        // Bool Query (ElasticSearch Query) - must, must_not, should, filter 조건 사용
        Query boolQuery = QueryBuilders.bool(b -> {
            // 검색 키워드로 스코어 기반 쿼리 검색
            if (keyword != null && !keyword.isBlank()) {
                b.must(m -> m.multiMatch(mm ->
                        mm.fields("lectureNm","professor").query(keyword)
                ));
            }
            // 소속에 따라서 필터링 적용
            if (department != null) {
                b.filter(f -> f
                        .term(t -> t.field("department").value(department.name()))
                );
            }
            return b;
        });

        // 정렬 옵션 선택 (별점 평점순, 좋아요순, 조회수순)
        SortOptions sortOpts = switch (sortOption) {
            case RATING -> SortOptions.of(o -> o.field(f -> f.field("starRating").order(SortOrder.Desc)));
            case LIKE -> SortOptions.of(o -> o.field(f -> f.field("likes").order(SortOrder.Desc)));
            case HIT -> SortOptions.of(o -> o.field(f -> f.field("hits").order(SortOrder.Desc)));
        };

        // NativeQuery 빌드
        NativeQuery query = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(pageNumber, size))
                .withSort(sortOpts)
                .build();

        // SearchHits
        SearchHits<LectureDocument> hits = elasticsearchOperations.search(query, LectureDocument.class, IndexCoordinates.of("lectures"));
        // SearchPage로 변환
        return SearchHitSupport.searchPageFor(hits, PageRequest.of(pageNumber, size));
    }
}

