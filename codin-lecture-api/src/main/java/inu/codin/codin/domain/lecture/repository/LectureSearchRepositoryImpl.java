package inu.codin.codin.domain.lecture.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import inu.codin.codin.domain.lecture.entity.*;
import inu.codin.codin.domain.lecture.exception.LectureErrorCode;
import inu.codin.codin.domain.lecture.exception.LectureException;
import inu.codin.codin.global.common.entity.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LectureSearchRepositoryImpl implements LectureSearchRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Lecture> search(String keyword, Department department, SortingOption sortingOption, Pageable pageable) {
        QLecture lecture = QLecture.lecture;
        QTag tag = QTag.tag;
        QLectureTag lectureTag = QLectureTag.lectureTag;

        BooleanBuilder builder = new BooleanBuilder();

        if (department != null) {
            validDepartment(department);
            builder.and(lecture.department.eq(department));
        }

        if (keyword != null && !keyword.isBlank()){
            builder.andAnyOf(
                    lecture.lectureNm.containsIgnoreCase(keyword),
                    lecture.lectureType.containsIgnoreCase(keyword),
                    lecture.evaluation.stringValue().containsIgnoreCase(keyword),
                    lecture.preCourse.containsIgnoreCase(keyword),
                    lecture.type.stringValue().containsIgnoreCase(keyword),
                    lecture.professor.containsIgnoreCase(keyword),
                    tag.tagName.containsIgnoreCase(keyword)
            );
        }

        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(lecture, sortingOption);
        List<Lecture> lectures = jpaQueryFactory
                .selectDistinct(lecture)
                .from(lecture)
                .leftJoin(lecture.tags, lectureTag)
                .leftJoin(lectureTag.tag, tag)
                .where(builder)
                .orderBy(orderSpecifier != null ? orderSpecifier : lecture.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(lecture.countDistinct())
                .from(lecture)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(lectures, pageable, total != null? total : 0);
    }

    private OrderSpecifier<?> getOrderSpecifier(QLecture lecture, SortingOption sortingOption) {
        if (sortingOption == null) return null;
        return switch (sortingOption) {
            case HIT -> lecture.hits.desc();
            case LIKE -> lecture.likes.desc();
            case RATING -> lecture.starRating.desc();
        };
    }

    private void validDepartment(Department department) {
        if (! (department.equals(Department.EMBEDDED) || department.equals(Department.COMPUTER_SCI) || department.equals(Department.INFO_COMM)))
            throw new LectureException(LectureErrorCode.DEPARTMENT_WRONG_INPUT);
    }
}
