package inu.codin.lecture.domain.elasticsearch.event;

import inu.codin.lecture.domain.lecture.entity.Lecture;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureSavedEvent {
    private Lecture lecture;
}
