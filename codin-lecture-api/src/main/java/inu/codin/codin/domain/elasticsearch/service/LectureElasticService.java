package inu.codin.codin.domain.elasticsearch.service;


import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import inu.codin.codin.domain.lecture.entity.SortingOption;
import inu.codin.codin.global.common.entity.Department;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LectureElasticService {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * ElasticSearch 강의 검색 메서드
     * @param keyword    키워드 (풀텍스트 검색)
     * @param department 학과 필터 (null 가능)
     * @param sortOption 정렬 옵션 (별점, 좋아요, 조회수)
     * @param likedIds   사용자가 좋아요 누른 강의 ID 목록 (null 또는 empty 시 무시)
     * @param pageNumber 0-based 페이지 번호
     * @param size       페이지 사이즈
     * @return Page<Long>
     */
    public Page<Long> searchIds(
            String keyword,
            Department department,
            SortingOption sortOption,
            List<Long> likedIds,
            int pageNumber,
            int size
    ) {
        log.info("강의 검색 - keyword={}, Department={}, sortingOption={}, likedIdsCount={}, pageNumber={}, size={}", keyword, department, sortOption, likedIds != null ? likedIds.size() : 0, pageNumber, size);

        Query boolQuery = QueryBuilders.bool(b -> {
            if (keyword != null && !keyword.isBlank()) {
                b.should(s -> s.multiMatch(mm -> mm
                                .fields("lectureNm^3", "tags^2", "professor", "type", "lectureType", "evaluation", "preCourses")
                                .query(keyword)
                                .operator(Operator.Or)
                        ))
                        .should(s -> s.wildcard(w -> w
                                .field("lectureNm")
                                .value("*" + keyword + "*")
                        ))
                        .should(s -> s.match(m -> m
                                .field("tags")
                                .query(keyword)
                        ))
                        .minimumShouldMatch("1"); // 최소 매칭 갯수
            } else {
                // 검색 키워드가 없으면 모든 문서 반환
                b.must(m -> m.matchAll(ma -> ma));
            }

            // 소속 별 검색
            if (department != null) {
                b.filter(f -> f.term(t -> t.field("department").value(department.name())));
            }
            // 좋아요 id
            if (likedIds != null && !likedIds.isEmpty()) {
                b.filter(f -> f.ids(i -> i.values(likedIds.stream().map(String::valueOf).toList())));
            }
            return b;
        });

        // 정렬 기준 옵션
        List<SortOptions> sortOptions = List.of(
                getSortOption(sortOption),
                SortOptions.of(o -> o.field(f -> f.field("starRating").order(SortOrder.Desc))),
                SortOptions.of(o -> o.field(f -> f.field("likes").order(SortOrder.Desc)))
        );

        // ES NativeQuery 생성
        NativeQuery query = NativeQuery.builder()
                .withQuery(boolQuery)
                .withSort(sortOptions)
                .withPageable(PageRequest.of(pageNumber, size))
                .build();

        // 검색 전 쿼리 로깅
        SearchHits<LectureDocument> hits = elasticsearchOperations.search(query, LectureDocument.class, IndexCoordinates.of("lectures"));
        log.info("Search returned {} hits out of {} total", hits.getSearchHits().size(), hits.getTotalHits());

        List<Long> ids = hits.getSearchHits()
                .stream()
                .map(hit -> hit.getContent().getId())
                .toList();

        return new PageImpl<>(ids,
                PageRequest.of(pageNumber, size),
                hits.getTotalHits());
    }

    private SortOptions getSortOption(SortingOption sortOption) {
        if (sortOption == null) {
            return SortOptions.of(o -> o.field(f -> f.field("starRating").order(SortOrder.Desc)));
        }

        return switch (sortOption) {
            case RATING -> SortOptions.of(o -> o.field(f -> f.field("starRating").order(SortOrder.Desc)));
            case LIKE   -> SortOptions.of(o -> o.field(f -> f.field("likes").order(SortOrder.Desc)));
            case HIT    -> SortOptions.of(o -> o.field(f -> f.field("hits").order(SortOrder.Desc)));
        };
    }
}

