package inu.codin.codin.domain.like.dto.event;

import inu.codin.codin.domain.like.entity.LikeType;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEvent;

@Getter
public class LikeNotificationEvent extends ApplicationEvent {
    private final LikeType likeType;

    private final ObjectId likeTypeId;

    public LikeNotificationEvent(Object source, LikeType likeType, ObjectId likeTypeId) {
        super(source);
        this.likeType = likeType;
        this.likeTypeId = likeTypeId;
    }
}
