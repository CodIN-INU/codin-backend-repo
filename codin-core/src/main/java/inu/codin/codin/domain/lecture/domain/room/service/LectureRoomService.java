package inu.codin.codin.domain.lecture.domain.room.service;

import inu.codin.codin.domain.lecture.domain.room.dto.EmptyRoomResponseDto;
import inu.codin.codin.domain.lecture.domain.room.entity.LectureRoomEntity;
import inu.codin.codin.domain.lecture.repository.LectureRepositoryCustomImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureRoomService {

    private final MongoTemplate mongoTemplate;

    public List<LectureRoomEntity> getLecturesByFloor(int floor) {

        AggregationOperation match = Aggregation.match(
                Criteria.where("roomNum").ne("")
        );
        String query =
                "{ $match: {" +
                        "$expr: {"+
                        "$and: ["+
                        "{ $ne: [ '$roomNum', ''] },"+
                        "{ $eq: [{ $floor: { $divide: [{ $toInt: '$roomNum' }, 100] } }, "+ floor+"] }]}}}";

        TypedAggregation<LectureRoomEntity> aggregation = Aggregation.newAggregation(
                LectureRoomEntity.class,
                match,
                new LectureRepositoryCustomImpl(query)
        );
        // Execute aggregation
        AggregationResults<LectureRoomEntity> results = mongoTemplate.aggregate(aggregation, LectureRoomEntity.class);
        return results.getMappedResults();
    }


    public List<Map<Integer, List<EmptyRoomResponseDto>>> statusOfEmptyRoom() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek today = now.getDayOfWeek();
        ArrayList<Map<Integer, List<EmptyRoomResponseDto>>> lectureRoom = new ArrayList<>();
        for (int floor=1; floor<=5; floor++) {
            List<LectureRoomEntity> roomEntities = getLecturesByFloor(floor);
            lectureRoom.add(roomEntities.stream()
                    .filter(lecture -> lecture.getDayTime().containsKey(today))
                    .flatMap(lecture -> lecture.getDayTime().get(today).stream()
                            .map(time -> EmptyRoomResponseDto.of(lecture, time)))
                    .collect(Collectors.groupingBy(
                            EmptyRoomResponseDto::getRoomNum
                    )));
        }
        return lectureRoom;
    }
}
