package inu.codin.codin.domain.lecture.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import inu.codin.codin.domain.elasticsearch.service.LectureElasticService;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.entity.QLecture;
import inu.codin.codin.domain.lecture.entity.Semester;
import inu.codin.codin.domain.lecture.entity.SortingOption;
import inu.codin.codin.domain.lecture.exception.LectureErrorCode;
import inu.codin.codin.domain.lecture.exception.LectureException;
import inu.codin.codin.global.common.entity.Department;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LectureSearchRepositoryImpl implements LectureSearchRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    private final LectureElasticService lectureElasticService;

    @Override
    @Transactional(readOnly = true)
    public Page<LectureDocument> searchLecturesAtPreview(String keyword, Department department, SortingOption sortingOption, List<Long> listList, Pageable pageable, Boolean like) {
        log.debug("[searchLecturesAtPreview] 강의 조회, keyword={}, department={}, sortingOption={}, liked={}", keyword, department, sortingOption, listList);
        Page<LectureDocument> lectureDocumentPage = lectureElasticService.searchLectureDocument(keyword, department, sortingOption, listList, pageable.getPageNumber(), pageable.getPageSize(), like);

        long total = lectureDocumentPage.getTotalElements();
        if (lectureDocumentPage.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        log.info("[searchLecturesAtPreview] 강의 조회, size={} ", lectureDocumentPage.getSize());

        // Page<Lecture> 변환하여 반환
        return new PageImpl<>(lectureDocumentPage.getContent(), pageable, total);
    }

    @Override
    public List<Lecture> searchLecturesAtReview(Department department, Integer grade, Semester semester) {
        QLecture lecture = QLecture.lecture;

        BooleanBuilder builder = new BooleanBuilder();
        searchByDepartment(department, builder, lecture);
        if (grade != null) { //학년이 있다면 조건 추가
            builder.and(lecture.grade.eq(grade));
        }

        JPQLQuery<Lecture> query = jpaQueryFactory
                .selectFrom(lecture);

        if (semester != null) { //학기가 있다면 semester에 대한 join 진행
            query.leftJoin(lecture.semester).fetchJoin();
            builder.and(lecture.semester.any().semester.eq(semester));
        }

        return query
                .where(builder)
                .fetch();
    }

    private void searchByDepartment(Department department, BooleanBuilder builder, QLecture lecture) {
        if (department != null) { //학과에 대한 정렬이 필요하다면 검증 후 조건 추가
            validDepartment(department);
            builder.and(lecture.department.eq(department));
        }
    }

    private void validDepartment(Department department) {
        if (! (department.equals(Department.EMBEDDED) || department.equals(Department.COMPUTER_SCI) || department.equals(Department.INFO_COMM)))
            throw new LectureException(LectureErrorCode.DEPARTMENT_WRONG_INPUT);
    }
}
