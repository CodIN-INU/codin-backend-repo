package inu.codin.codin.domain.elasticsearch.repository;

import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import inu.codin.codin.domain.lecture.entity.SortingOption;
import inu.codin.common.entity.Department;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;

public interface LectureElasticRepository {
    String INDEX_NAME = "lectures";

    void incrementHit(Long lectureId) throws IOException;

    void updateStarRating(Long lectureId, Double newRating) throws IOException;

    void incrementLike(Long lectureId) throws IOException;

    void decrementLike(Long lectureId) throws IOException;

    void createLectureIndex() throws IOException;

    void deleteLectureIndex() throws IOException;

    boolean isLectureIndexExist() throws IOException;

    void bulkIndexLectures(List<LectureDocument> documents);

    void saveLecture(LectureDocument document) throws IOException;

    void deleteLecture(Long lectureId) throws IOException;

    Page<LectureDocument> searchLectureDocumentList(String keyword, Department department, SortingOption sortOption, List<String> likeIdList, int pageNumber, int size, Boolean like) throws IOException;
}
