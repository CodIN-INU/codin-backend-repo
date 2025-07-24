package inu.codin.codin.domain.elastic.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureDeletedEvent {
    private Long lectureId;
}
