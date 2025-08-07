package inu.codin.codin.domain.lecture.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import inu.codin.codin.domain.elasticsearch.service.LectureElasticService;
import inu.codin.codin.domain.lecture.entity.*;
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
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LectureSearchRepositoryImpl implements LectureSearchRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    private final LectureElasticService lectureElasticService;
    private final LectureRepository lectureRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Lecture> searchLecturesAtPreview(String keyword, Department department, SortingOption sortingOption, List<Long> listList, Pageable pageable, Boolean like) {
        log.debug("[searchLecturesAtPreview] 강의 조회, keyword={}, department={}, sortingOption={}, liked={}", keyword, department, sortingOption, listList);
        Page<Long> idPage = lectureElasticService.searchIds(keyword, department, sortingOption, listList, pageable.getPageNumber(), pageable.getPageSize(), like);

        List<Long> ids = idPage.getContent();
        long total = idPage.getTotalElements();
        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        // JPA로 Lecture 엔티티 Fetch 조회
        List<Lecture> lectures = lectureRepository.findAllWithAssociationsByIds(ids);
        log.info("[searchLecturesAtPreview] 강의 조회, size={} ", lectures.size());

        // ES 정렬 순서 유지하며 재정렬
        Map<Long, Lecture> lectureMap = lectures.stream()
                .collect(Collectors.toMap(Lecture::getId, Function.identity()));
        List<Lecture> ordered = ids.stream()
                .map(lectureMap::get)
                .filter(Objects::nonNull)
                .toList();

        // Page<Lecture> 변환하여 반환
        return new PageImpl<>(ordered, pageable, total);
    }

    @Deprecated
    private void applyTagJoins(JPQLQuery<?> query, QLecture lecture, QLectureTag lectureTag, QTag tag) {
        query.leftJoin(lecture.tags, lectureTag)
                .leftJoin(lectureTag.tag, tag);
    }

    @Deprecated
    private void addUserLikedFilter(List<Long> liked, BooleanBuilder builder, QLecture lecture) {
        if (liked != null && !liked.isEmpty()) {
            builder.and(lecture.id.in(liked));
        }
    }

    @Deprecated
    private void searchByKeyword(String keyword, BooleanBuilder builder, QLecture lecture, QLectureTag lectureTag) {
        if (keyword != null && !keyword.isBlank()){ //키워드가 있다면 모든 정보에 대해서 확인
            builder.andAnyOf(
                    lecture.lectureNm.containsIgnoreCase(keyword),
                    lecture.lectureType.containsIgnoreCase(keyword),
                    lecture.evaluation.stringValue().containsIgnoreCase(keyword),
                    lecture.preCourse.containsIgnoreCase(keyword),
                    lecture.type.stringValue().containsIgnoreCase(keyword),
                    lecture.professor.containsIgnoreCase(keyword),
                    lectureTag.tag.tagName.containsIgnoreCase(keyword)
                    //todo 이후에 추가될 수 있는 필드들에 대해서도 조건 추가
            );
        }
    }

    @Deprecated
    private OrderSpecifier[] getOrderSpecifier(QLecture lecture, SortingOption sortingOption) {
        if (sortingOption == null) {
            return new OrderSpecifier[]{lecture.starRating.desc(),
                    lecture.likes.desc(),
                    lecture.hits.desc()}; // 기본 정렬
        }

        return switch (sortingOption) {
            case HIT -> new OrderSpecifier[]{
                    lecture.hits.desc(),
                    lecture.starRating.desc(),
                    lecture.likes.desc()
            };
            case LIKE -> new OrderSpecifier[]{
                    lecture.likes.desc(),
                    lecture.starRating.desc(),
                    lecture.hits.desc()
            };
            case RATING -> new OrderSpecifier[]{
                    lecture.starRating.desc(),
                    lecture.likes.desc(),
                    lecture.hits.desc()
            };
        };
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
