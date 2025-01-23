package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.common.Department;
import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.lecture.dto.EmptyRoomResponseDto;
import inu.codin.codin.domain.lecture.dto.LectureListResponseDto;
import inu.codin.codin.domain.lecture.dto.LecturePageResponse;
import inu.codin.codin.domain.lecture.dto.Option;
import inu.codin.codin.domain.lecture.entity.LectureEntity;
import inu.codin.codin.domain.lecture.exception.WrongInputException;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import inu.codin.codin.domain.lecture.repository.LectureRepositoryCustomImpl;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    public LecturePageResponse sortListOfLectures(Department department, Option option, int page) {
        if (department.equals(Department.EMBEDDED) || department.equals(Department.COMPUTER_SCI) || department.equals(Department.INFO_COMM) || department.equals(Department.OTHERS)) {
            PageRequest pageRequest = PageRequest.of(page, 20, option==Option.LEC? Sort.by("lectureNm"):Sort.by("professor"));
            Page<LectureEntity> lecturePage = lectureRepository.findAllByDepartment(pageRequest, department);
            return LecturePageResponse.of(lecturePage.stream().map(LectureListResponseDto::of).toList(),
                    lecturePage.getTotalPages() -1,
                    lecturePage.hasNext()? lecturePage.getPageable().getPageNumber() + 1: -1);
        } else throw new WrongInputException("학과명을 잘못 입력하였습니다. department: " + department.getDescription());
    }

    public Object searchLectures(String keyword, int page) {
        PageRequest pageRequest = PageRequest.of(page, 20, Sort.by("lectureNm"));
        Page<LectureEntity> lecturePage = lectureRepository.findAllByKeyword(keyword, pageRequest);
        return LecturePageResponse.of(
                lecturePage.stream().map(LectureListResponseDto::of).toList(),
                lecturePage.getTotalPages() - 1,
                lecturePage.hasNext()? lecturePage.getPageable().getPageNumber() + 1 : -1
        );
    }
}
