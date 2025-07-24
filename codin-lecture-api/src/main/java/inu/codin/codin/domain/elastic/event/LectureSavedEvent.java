package inu.codin.codin.domain.elastic.event;

import inu.codin.codin.domain.lecture.entity.Lecture;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureSavedEvent {
    private Lecture lecture;
}
