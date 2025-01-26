package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.common.Department;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final RedisReviewService redisReviewService;

    public LectureDetailResponseDto getLectureDetails(String lectureId) {
        LectureEntity lectureEntity = lectureRepository.findById(new ObjectId(lectureId))
                .orElseThrow(() -> new NotFoundException("강의 정보를 찾을 수 없습니다."));
        double ave = redisReviewService.getAveOfRating(lectureId);
        Emotion emotion = redisReviewService.getEmotionRating(lectureId);
        long participants = redisReviewService.getParticipants(lectureId);
        return LectureDetailResponseDto.of(lectureEntity, ave, emotion, participants);
    }

    public LecturePageResponse sortListOfLectures(Department department, Option option, int page) {
        if (department.equals(Department.EMBEDDED) || department.equals(Department.COMPUTER_SCI) || department.equals(Department.INFO_COMM) || department.equals(Department.OTHERS)) {
            PageRequest pageRequest = PageRequest.of(page, 20, option==Option.LEC? Sort.by("lectureNm"):Sort.by("professor"));
            Page<LectureEntity> lecturePage = lectureRepository.findAllByDepartment(pageRequest, department);
            return LecturePageResponse.of(lecturePage.stream()
                            .map(lecture -> LectureListResponseDto.of(lecture,
                                    redisReviewService.getAveOfRating(lecture.get_id().toString()),
                                    redisReviewService.getParticipants(lecture.get_id().toString()) ))
                            .toList(),
                    lecturePage.getTotalPages() -1,
                    lecturePage.hasNext()? lecturePage.getPageable().getPageNumber() + 1: -1);
        } else throw new WrongInputException("학과명을 잘못 입력하였습니다. department: " + department.getDescription());
    }

    public Object searchLectures(String keyword, int page) {
        PageRequest pageRequest = PageRequest.of(page, 20, Sort.by("lectureNm"));
        Page<LectureEntity> lecturePage = lectureRepository.findAllByKeyword(keyword, pageRequest);
        return LecturePageResponse.of(lecturePage.stream()
                        .map(lecture -> LectureListResponseDto.of(lecture,
                                redisReviewService.getAveOfRating(lecture.get_id().toString()),
                                redisReviewService.getParticipants(lecture.get_id().toString()) ))
                        .toList(),
                lecturePage.getTotalPages() - 1,
                lecturePage.hasNext()? lecturePage.getPageable().getPageNumber() + 1 : -1
        );
    }
}
