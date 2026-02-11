package inu.codin.codin.domain.elasticsearch.service;


import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import inu.codin.codin.domain.elasticsearch.repository.LectureElasticRepository;
import inu.codin.codin.domain.lecture.entity.SortingOption;
import inu.codin.common.entity.Department;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LectureElasticService {
    private final LectureElasticRepository lectureElasticRepository;

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
    public Page<LectureDocument> searchLectureDocumentList(String keyword, Department department, SortingOption sortOption, List<String> likeIdList, int pageNumber, int size, Boolean like) {
        try {
            return lectureElasticRepository.searchLectureDocumentList(keyword, department, sortOption, likeIdList, pageNumber, size, like);
        } catch (IOException e) {
            log.error("검색에 실패했습니다. : {}", e.getMessage());

            return new PageImpl<>(new ArrayList<>(), PageRequest.of(pageNumber, size), 0);
        }
    }

    public void incrementHit(Long lectureId) {
        try {
            lectureElasticRepository.incrementHit(lectureId);
        } catch (IOException e) {
            log.error("조회수 업데이트 실패 lectureId={}, {}", lectureId, e.getMessage());
        }
    }

    public void updateStarRating(Long lectureId, Double newRating) {
        try {
            lectureElasticRepository.updateStarRating(lectureId, newRating);
        } catch (IOException e) {
            log.error("별점 업데이트 실패. lectureId={}, newRating={}, {}", lectureId, newRating, e.getMessage());
        }
    }

    public void incrementLike(Long lectureId) {
        try {
            lectureElasticRepository.incrementLike(lectureId);
        } catch (IOException e) {
            log.error("좋아요 업데이트 실패 lectureId={}, {}", lectureId, e.getMessage());
        }
    }

    public void decrementLike(Long lectureId) {
        try {
            lectureElasticRepository.decrementLike(lectureId);
        } catch (IOException e) {
            log.error("좋아요 업데이트 실패 lectureId={}, {}", lectureId, e.getMessage());
        }
    }
}

