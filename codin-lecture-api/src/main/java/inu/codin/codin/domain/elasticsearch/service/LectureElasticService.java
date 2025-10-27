package inu.codin.codin.domain.elasticsearch.service;


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
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ScriptType;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LectureElasticService {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * ElasticSearch 강의 검색 메서드
     *
     * @param keyword    키워드 (풀텍스트 검색)
     * @param department 학과 필터 (null 가능)
     * @param sortOption 정렬 옵션 (별점, 좋아요, 조회수)
     * @param likeIdList 사용자가 좋아요 누른 강의 ID 목록 (null 또는 empty 시 무시)
     * @param pageNumber 0-based 페이지 번호
     * @param size       페이지 사이즈
     * @return Page<Long>
     */
    public Page<LectureDocument> searchLectureDocument(
            String keyword,
            Department department,
            SortingOption sortOption,
            List<String> likeIdList,
            int pageNumber,
            int size,
            Boolean like
    ) {
        log.info("강의 검색 - keyword={}, Department={}, sortingOption={}, likedIdsCount={}, pageNumber={}, size={}", keyword, department, sortOption, likeIdList != null ? likeIdList.size() : 0, pageNumber, size);

        if (Boolean.TRUE.equals(like) && (likeIdList == null || likeIdList.isEmpty())) {
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(pageNumber, size), 0);
        }

        Query boolQuery = QueryBuilders.bool(b -> {
            if (keyword != null && !keyword.isBlank()) {
                b.must(m -> m.multiMatch(mm -> mm
                        .fields("lectureNm^3", "tags^2", "professor", "type", "lectureType", "evaluation")
                        .query(keyword)
                        .operator(Operator.Or)
                ));
            } else {
                b.must(m -> m.matchAll(ma -> ma));
            }

            // 소속 별 검색
            if (department != null) {
                b.filter(f -> f.term(t -> t.field("department").value(department.name())));
            }

            if (Boolean.TRUE.equals(like) && likeIdList != null && !likeIdList.isEmpty()) {
                b.filter(f -> f.ids(i -> i.values(likeIdList)));
            }


            return b;
        });

        // 정렬 기준 옵션
        Sort sortOptions = createSortOptions(sortOption);

        // ES NativeQuery 생성
        NativeQuery query = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(pageNumber, size))
                .withSort(sortOptions)
                .build();

        // 검색 전 쿼리 로깅
        SearchHits<LectureDocument> hits = elasticsearchOperations.search(query, LectureDocument.class, IndexCoordinates.of("lectures"));
        List<LectureDocument> lectureDocumentList = hits.getSearchHits().stream().map(SearchHit::getContent).toList();
        log.info("Search returned {} hits out of {} total", hits.getSearchHits().size(), hits.getTotalHits());

        return new PageImpl<>(lectureDocumentList,
                PageRequest.of(pageNumber, size),
                hits.getTotalHits());
    }

    public void incrementHits(Long lectureId) {
        Map<String, Object> params = Map.of("inc", 1);
        String script = "ctx._source.hits = (ctx._source.hits != null ? ctx._source.hits : 0) + params.inc";

        UpdateQuery uq = UpdateQuery.builder(lectureId.toString())
                .withScriptType(ScriptType.INLINE)
                .withScript(script)
                .withLang("painless")
                .withParams(params)
                .build();

        elasticsearchOperations.update(uq, IndexCoordinates.of("lectures"));
        log.debug("Incremented hits for lectureId={}", lectureId);
    }

    public void updateStarRating(Long lectureId, Double newRating) {
        Map<String, Object> params = Map.of("newRating", newRating);

        String script = "ctx._source.starRating = params.newRating";

        UpdateQuery updateQuery = UpdateQuery.builder(lectureId.toString())
                .withScriptType(ScriptType.INLINE)
                .withScript(script)
                .withLang("painless")
                .withParams(params)
                .build();

        elasticsearchOperations.update(updateQuery, IndexCoordinates.of("lectures"));
        log.debug("Updated starRating for lectureId={} to {}", lectureId, newRating);
    }

    public void incrementLikes(Long lectureId) {
        Map<String, Object> params = Map.of("inc", 1);

        String script = "ctx._source.likes = (ctx._source.likes != null ? ctx._source.likes : 0) + params.inc";

        UpdateQuery updateQuery = UpdateQuery.builder(lectureId.toString())
                .withScriptType(ScriptType.INLINE)
                .withScript(script)
                .withLang("painless")
                .withParams(params)
                .build();

        elasticsearchOperations.update(updateQuery, IndexCoordinates.of("lectures"));
        log.debug("Incremented likes for lectureId={}", lectureId);
    }

    public void decrementLikes(Long lectureId) {
        Map<String, Object> params = Map.of("dec", 1);

        String script = "if (ctx._source.likes != null && ctx._source.likes > 0) { ctx._source.likes -= params.dec; } else { ctx._source.likes = 0; }";

        UpdateQuery updateQuery = UpdateQuery.builder(lectureId.toString())
                .withScriptType(ScriptType.INLINE)
                .withScript(script)
                .withLang("painless")
                .withParams(params)
                .build();

        elasticsearchOperations.update(updateQuery, IndexCoordinates.of("lectures"));
        log.debug("Decremented likes for lectureId={}", lectureId);
    }

    /**
     * SortingOption 이 null 일 경우 score 순 정렬
     * null 아닐 경우 primaryOption 이 가장 앞. 나머지는 enum 선언 순서를 유지.
     */
    private Sort createSortOptions(SortingOption primaryOption) {
        if (primaryOption == null) {

            return Sort.unsorted();
        }

        List<Sort.Order> orders = Arrays.stream(SortingOption.values())
                .sorted((o1, o2) -> o1 == primaryOption ? -1 : (o2 == primaryOption ? 1 : 0))
                .map(this::getSortOption)
                .toList();

        return Sort.by(orders);
    }

    private Sort.Order getSortOption(SortingOption sortOption) {
        return switch (sortOption) {
            case RATING -> Sort.Order.desc("starRating");
            case LIKE -> Sort.Order.desc("likes");
            case HIT -> Sort.Order.desc("hits");
        };
    }
}

