package inu.codin.codin.domain.chat.chatting;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Setter
public class Message {

    @Id @NotBlank
    private String messageId;

    private String senderId;

    private String content;

    private MessageType messageType;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
}
