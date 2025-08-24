package inu.codin.codin.domain.calendar.service;

import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.calendar.dto.*;
import inu.codin.codin.domain.calendar.entity.CalendarEntity;
import inu.codin.codin.domain.calendar.exception.CalendarErrorCode;
import inu.codin.codin.domain.calendar.exception.CalendarException;
import inu.codin.codin.domain.calendar.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;

    public CalendarMonthResponse getMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new CalendarException(CalendarErrorCode.DATE_FORMAT_ERROR);
        }

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<CalendarEntity> calendarEventList = calendarRepository
                .findByEndDateGreaterThanEqualAndStartDateLessThanEqual(monthStart, monthEnd).stream()
                .filter(e -> e.getStartDate() != null && e.getEndDate() != null)
                .toList();

        // 날짜 별로 클램핑
        Map<LocalDate, List<CalendarEntity>> days = new HashMap<>();
        for (CalendarEntity e : calendarEventList) {
            LocalDate startDate = e.getStartDate().isBefore(monthStart) ? monthStart : e.getStartDate();
            LocalDate endDate = e.getEndDate().isAfter(monthEnd) ? monthEnd : e.getEndDate();
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                days.computeIfAbsent(date, k -> new ArrayList<>()).add(e);
            }
        }

        // 날짜 별로 정렬하기
        List<CalendarDayResponse> dayResponses = new ArrayList<>();
        for (LocalDate date = monthStart; !date.isAfter(monthEnd); date = date.plusDays(1)) {
            List<CalendarEntity> list = new ArrayList<>(days.getOrDefault(date, Collections.emptyList()));
            list.sort(Comparator.comparing(CalendarEntity::getStartDate));

            int total = list.size();
            List<EventDto> events = list.stream()
                    .map(EventDto::of)
                    .toList();
            dayResponses.add(new CalendarDayResponse(date, total, events));
        }
        return CalendarMonthResponse.builder()
                .year(year)
                .month(month)
                .days(dayResponses)
                .build();
    }

    public CalendarCreateResponse create(CalendarCreateRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new CalendarException(CalendarErrorCode.DATE_CANNOT_NULL);
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new CalendarException(CalendarErrorCode.DATE_FORMAT_ERROR);
        }

        CalendarEntity entity = CalendarEntity.builder()
                .content(request.getContent())
                .department(request.getDepartment())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        CalendarEntity savedEntity = calendarRepository.save(entity);
        return CalendarCreateResponse.of(savedEntity);
    }

    public void delete(String id) {
        ObjectId objectId = ObjectIdUtil.toObjectId(id);
        CalendarEntity calendar = calendarRepository.findByIdAndNotDeleted(objectId)
                .orElseThrow(() -> new CalendarException(CalendarErrorCode.CALENDAR_EVENT_NOT_FOUND));
        calendar.delete();
        calendarRepository.save(calendar);
    }
}
