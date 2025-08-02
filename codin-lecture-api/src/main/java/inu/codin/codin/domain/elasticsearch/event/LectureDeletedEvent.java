package inu.codin.codin.domain.elasticsearch.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureDeletedEvent {
    private Long lectureId;
}
