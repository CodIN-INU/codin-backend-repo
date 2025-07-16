package inu.codin.codin.domain.report.repository;

import inu.codin.codin.domain.report.entity.ReportEntity;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomReportRepository {

    private final MongoTemplate mongoTemplate;

    public CustomReportRepository(MongoTemplate mongoTemplate) {

        this.mongoTemplate = mongoTemplate;
    }

    public List<Document> findPendingReportsOrderedGroupedBy() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("reportStatus").is("PENDING")),  // 'PENDING' 상태인 항목만 필터링
                Aggregation.group("reportTargetId")  // reportTargetId 기준으로 그룹화
                        .count().as("count")  // 신고 개수 카운팅
                        .push("$$ROOT").as("reports"),  // 신고 목록을 reports 필드로 밀어넣기
                Aggregation.sort(Sort.by(Sort.Order.desc("count")))  // 신고 개수 기준 내림차순 정렬
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, ReportEntity.class, Document.class);
        return results.getMappedResults();  // Document로 결과 반환
    }
}
