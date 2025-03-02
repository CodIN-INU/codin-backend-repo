package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.lecture.dto.*;
import inu.codin.codin.domain.lecture.entity.LectureEntity;
import inu.codin.codin.domain.lecture.exception.WrongInputException;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import inu.codin.codin.infra.redis.service.RedisReviewService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final RedisReviewService redisReviewService;
    private final MongoTemplate mongoTemplate;

    public LectureDetailResponseDto getLectureDetails(String lectureId) {
        LectureEntity lectureEntity = lectureRepository.findById(new ObjectId(lectureId))
                .orElseThrow(() -> new NotFoundException("강의 정보를 찾을 수 없습니다."));
        double ave = redisReviewService.getAveOfRating(lectureId);
        Emotion emotion = redisReviewService.getEmotionRating(lectureId);
        long participants = redisReviewService.getParticipants(lectureId);
        return LectureDetailResponseDto.of(lectureEntity, ave, emotion, participants);
    }

    public LecturePageResponse sortListOfLectures(Department department, String keyword, Option option, int page) {
        if (department.equals(Department.EMBEDDED) || department.equals(Department.COMPUTER_SCI) || department.equals(Department.INFO_COMM) || department.equals(Department.OTHERS)) {
            PageRequest pageRequest = PageRequest.of(page, 20, option==Option.LEC? Sort.by("lectureNm"):Sort.by("professor"));
            Page<LectureEntity> lecturePage;
            if (keyword == null){
                lecturePage = lectureRepository.findAllByDepartment(pageRequest, department);
            } else {
                lecturePage = lectureRepository.findAllByKeywordAndDepartment(department, keyword, pageRequest);
            }
            return getLecturePageResponse(lecturePage);
        } else throw new WrongInputException("학과명을 잘못 입력하였습니다. department: " + department.getDescription());
    }

    public List<LectureSearchListResponseDto> searchLecturesToReview(Department department, Integer grade, String semester) {
        List<LectureEntity> lectures = findLectures(department, grade, semester);
        if (semester != null) return lectures.stream()
                                    .map(lecture -> LectureSearchListResponseDto.of(lecture, semester))
                                    .toList();

        else return lectures.stream()
                    .map(LectureSearchListResponseDto::of)
                    .flatMap(List::stream)
                    .toList();
    }

    private LecturePageResponse getLecturePageResponse(Page<LectureEntity> lecturePage) {
        return LecturePageResponse.of(lecturePage.stream()
                        .map(lecture -> LectureListResponseDto.of(lecture,
                                redisReviewService.getAveOfRating(lecture.get_id().toString()),
                                redisReviewService.getParticipants(lecture.get_id().toString())))
                        .toList(),
                lecturePage.getTotalPages() - 1,
                lecturePage.hasNext() ? lecturePage.getPageable().getPageNumber() + 1 : -1);
    }

    public List<LectureEntity> findLectures(Department department, Integer grade, String semester) {
        Query query = new Query();

        if (department != null) {
            query.addCriteria(Criteria.where("department").is(department));
        }

        if (grade != null && grade > 0) {
            query.addCriteria(Criteria.where("grade").is(grade));
        }

        if (semester != null) {
            query.addCriteria(Criteria.where("semester").in(List.of(semester)));
        }

        query.with(Sort.by(Sort.Direction.ASC, "lectureNm"));

        return mongoTemplate.find(query, LectureEntity.class);
    }
}
