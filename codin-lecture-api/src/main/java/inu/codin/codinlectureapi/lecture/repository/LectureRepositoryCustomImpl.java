package inu.codin.codinlectureapi.lecture.repository;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LectureRepositoryCustomImpl implements AggregationOperation{

    private String jsonOperation;

    public LectureRepositoryCustomImpl(String jsonOperation) {
        this.jsonOperation = jsonOperation;
    }

    @Override
    public Document toDocument(AggregationOperationContext context) {
        return context.getMappedObject(Document.parse(jsonOperation));
    }
}
