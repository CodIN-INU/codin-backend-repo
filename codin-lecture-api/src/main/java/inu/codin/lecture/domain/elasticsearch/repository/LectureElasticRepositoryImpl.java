package inu.codin.lecture.domain.elasticsearch.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import inu.codin.lecture.domain.elasticsearch.document.LectureDocument;
import inu.codin.lecture.domain.elasticsearch.document.LectureIndexMapping;
import inu.codin.lecture.domain.lecture.entity.SortingOption;
import inu.codin.common.entity.Department;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LectureElasticRepositoryImpl implements LectureElasticRepository {
    private final ElasticsearchClient client;
    private final BulkIngester<BulkOperation> bulkIngester;

    @Override
    public void createLectureIndex() throws IOException {
        CreateIndexRequest request = LectureIndexMapping.createLectureIndexRequest();
        client.indices().create(request);

        log.info(INDEX_NAME + " 인덱스 생성 완료");
    }

    @Override
    public void deleteLectureIndex() throws IOException {
        client.indices().delete(d -> d
                .index(INDEX_NAME));
    }

    @Override
    public boolean isLectureIndexExist() throws IOException {
        BooleanResponse response = client.indices().exists(exist -> exist
                .index(INDEX_NAME));

        return response.value();
    }

    @Override
    public void incrementHit(Long lectureId) throws IOException {
        String script = "ctx._source.hits = (ctx._source.hits != null ? ctx._source.hits : 0) + params.inc";
        Map<String, JsonData> params = Map.of("inc", JsonData.of(1));

        UpdateRequest<LectureDocument, Object> request = new UpdateRequest.Builder<LectureDocument, Object>()
                .index(INDEX_NAME)
                .id(lectureId.toString())
                .script(s -> s
                        .lang("painless")
                        .source(script)
                        .params(params)
                )
                .build();

        client.update(request, LectureDocument.class);
    }

    @Override
    public void updateStarRating(Long lectureId, Double newRating) throws IOException {
        String script = "ctx._source.starRating = params.newRating";
        Map<String, JsonData> params = Map.of("newRating", JsonData.of(newRating));

        UpdateRequest<LectureDocument, Object> request = new UpdateRequest.Builder<LectureDocument, Object>()
                .index(INDEX_NAME)
                .id(lectureId.toString())
                .script(s -> s
                        .lang("painless")
                        .source(script)
                        .params(params)
                )
                .build();

        client.update(request, LectureDocument.class);
    }

    @Override
    public void incrementLike(Long lectureId) throws IOException {
        String script = "ctx._source.likes = (ctx._source.likes != null ? ctx._source.likes : 0) + params.inc";
        Map<String, JsonData> params = Map.of("inc", JsonData.of(1));

        UpdateRequest<LectureDocument, Object> request = new UpdateRequest.Builder<LectureDocument, Object>()
                .index(INDEX_NAME)
                .id(lectureId.toString())
                .script(s -> s
                        .lang("painless")
                        .source(script)
                        .params(params)
                )
                .build();

        client.update(request, LectureDocument.class);
    }

    @Override
    public void decrementLike(Long lectureId) throws IOException {
        String script = "if (ctx._source.likes != null && ctx._source.likes > 0) { ctx._source.likes -= params.dec; } else { ctx._source.likes = 0; }";
        Map<String, JsonData> params = Map.of("dec", JsonData.of(1));

        UpdateRequest<LectureDocument, Object> request = new UpdateRequest.Builder<LectureDocument, Object>()
                .index(INDEX_NAME)
                .id(lectureId.toString())
                .script(s -> s
                        .lang("painless")
                        .source(script)
                        .params(params)
                )
                .build();

        client.update(request, LectureDocument.class);
    }

    @Override
    public Page<LectureDocument> searchLectureDocumentList(String keyword, Department department, SortingOption sortOption, List<String> likeIdList, int pageNumber, int size, Boolean like) throws IOException {
        log.info("강의 검색 (Java Client) - keyword={}, Department={}, sortingOption={}, likedIdsCount={}, pageNumber={}, size={}",
                keyword, department, sortOption, likeIdList != null ? likeIdList.size() : 0, pageNumber, size);

        if (Boolean.TRUE.equals(like) && (likeIdList == null || likeIdList.isEmpty())) {
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(pageNumber, size), 0);
        }

        Query boolQuery = buildBoolQuery(keyword, department, likeIdList, like);
        List<SortOptions> sortOptions = createSortOptions(sortOption);

        int from = pageNumber * size;

        // 강의를 무한스크롤로 구현한다면 searchAfter로 변경 필요
        SearchRequest request = new SearchRequest.Builder()
                .index(INDEX_NAME)
                .query(boolQuery)
                .sort(sortOptions)
                .from(from)
                .size(size)
                .build();

        SearchResponse<LectureDocument> response = client.search(request, LectureDocument.class);

        List<LectureDocument> lectureDocumentList = response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());

        // 7. PageImpl 객체로 래핑하여 반환
        return new PageImpl<>(lectureDocumentList);
    }

    @Override
    public void saveLecture(LectureDocument document) throws IOException {
        if (document.getId() == null) {
            throw new IllegalArgumentException("Document ID cannot be null for saving.");
        }

        IndexRequest<LectureDocument> request = IndexRequest.of(index -> index
                .index(INDEX_NAME)
                .id(document.getId().toString())
                .document(document));

        client.index(request);
    }

    @Override
    public void deleteLecture(Long lectureId) throws IOException {
        DeleteRequest request = DeleteRequest.of(delete -> delete
                .index(INDEX_NAME)
                .id(lectureId.toString()));

        client.delete(request);
    }

    @Override
    public void bulkIndexLectures(List<LectureDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("강의리스트가 비어있습니다.");

            return;
        }

        log.info("강의 벌크 색인 시작: {}", documents.size());

        for (LectureDocument doc : documents) {
            if (doc.getId() == null) {
                log.warn("Skipping document with null ID (LectureNm: {})", doc.getLectureName());
                continue;
            }

            String docId = doc.getId().toString();

            bulkIngester.add(op -> op
                    .index(idx -> idx
                            .index(INDEX_NAME)
                            .id(docId)
                            .document(doc)
                    )
            );
        }

        log.info("{}개 문서 bulk ingester queue에 추가", documents.size());
    }

    private Query buildBoolQuery(String keyword, Department department, List<String> likeIdList, Boolean like) {
        return new Query.Builder()
                .bool(b -> {
                    if (keyword != null && !keyword.isBlank()) {
                        b.must(m -> m
                                .multiMatch(mm -> mm
                                        .fields("lectureName^3", "tags^2", "professor", "type", "lectureType", "evaluation")
                                        .query(keyword)
                                        .operator(Operator.Or)
                                )
                        );
                    } else {
                        b.must(m -> m.matchAll(ma -> ma));
                    }

                    if (department != null) {
                        b.filter(f -> f
                                .term(t -> t
                                        .field("department")
                                        .value(v -> v.stringValue(department.name()))
                                )
                        );
                    }

                    if (Boolean.TRUE.equals(like) && likeIdList != null && !likeIdList.isEmpty()) {
                        b.filter(f -> f
                                .ids(i -> i.values(likeIdList))
                        );
                    }

                    return b;
                })
                .build();
    }

    /**
     * SortingOption 이 null 일 경우 score 순 정렬
     * null 아닐 경우 primaryOption 이 가장 앞. 나머지는 enum 선언 순서를 유지.
     */
    private List<SortOptions> createSortOptions(SortingOption primaryOption) {
        if (primaryOption == null) {

            return new ArrayList<>();
        }

        return Arrays.stream(SortingOption.values())
                .sorted((o1, o2) -> {
                    if (o1 == primaryOption) return -1;
                    if (o2 == primaryOption) return 1;
                    return 0;
                })
                .map(this::getSortOption)
                .collect(Collectors.toList());
    }

    private SortOptions getSortOption(SortingOption sortOption) {

        return switch (sortOption) {
            case HIT -> SortOptions.of(s -> s
                    .field(f -> f.field("hits").order(SortOrder.Desc))
            );
            case LIKE -> SortOptions.of(s -> s
                    .field(f -> f.field("likes").order(SortOrder.Desc))
            );
            case RATING -> SortOptions.of(s -> s
                    .field(f -> f.field("starRating").order(SortOrder.Desc))
            );
        };
    }
}
