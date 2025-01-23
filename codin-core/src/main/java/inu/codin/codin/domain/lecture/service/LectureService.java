package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.lecture.dto.EmptyRoomResponseDto;
import inu.codin.codin.domain.lecture.entity.LectureEntity;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import inu.codin.codin.domain.lecture.repository.LectureRepositoryCustomImpl;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final MongoTemplate mongoTemplate;

    public List<LectureEntity> getLecturesByFloor(int floor) {

        AggregationOperation match = Aggregation.match(
                Criteria.where("roomNum").ne("")
        );
        String query =
                "{ $match: {" +
                    "$expr: {"+
                        "$and: ["+
                            "{ $ne: [ '$roomNum', ''] },"+
                            "{ $eq: [{ $floor: { $divide: [{ $toInt: '$roomNum' }, 100] } }, "+ floor+"] }]}}}";

        TypedAggregation<LectureEntity> aggregation = Aggregation.newAggregation(
                LectureEntity.class,
                match,
                new LectureRepositoryCustomImpl(query)
        );
        // Execute aggregation
        AggregationResults<LectureEntity> results = mongoTemplate.aggregate(aggregation, LectureEntity.class);
        return results.getMappedResults();
    }

    public void getLectureDetails(String lectureId) {
        LectureEntity lectureEntity = lectureRepository.findById(new ObjectId(lectureId))
                .orElseThrow(() -> new NotFoundException("강의 정보를 찾을 수 없습니다."));

    }

    public Map<Integer, List<EmptyRoomResponseDto>> statusOfEmptyRoom(int floor) {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek today = now.getDayOfWeek();
        List<LectureEntity> lectureEntity = getLecturesByFloor(floor);
        return lectureEntity.stream()
                .filter(lecture -> lecture.getDayTime().containsKey(today))
                .flatMap(lecture -> lecture.getDayTime().get(today).stream()
                        .map(time -> EmptyRoomResponseDto.of(lecture, time)))
                .collect(Collectors.groupingBy(
                        EmptyRoomResponseDto::getRoomNum
                ));
    }
}
