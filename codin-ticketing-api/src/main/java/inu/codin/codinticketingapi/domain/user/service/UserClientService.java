package inu.codin.codinticketingapi.domain.user.service;

import inu.codin.codinticketingapi.domain.user.dto.UserReply;
import inu.codin.codinticketingapi.domain.user.dto.UserRequest;
import inu.codin.codinticketingapi.domain.user.exception.UserErrorCode;
import inu.codin.codinticketingapi.domain.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserClientService {

    private final ReplyingKafkaTemplate<String, UserRequest, UserReply> kafkaTemplate;

    public UserReply fetchUserIdAndUsername(String email) {
        String requestId = UUID.randomUUID().toString();
        UserRequest req = new UserRequest(requestId, email);
        // 토픽에 보낼 레코드 생성
        ProducerRecord<String, UserRequest> record = new ProducerRecord<>("ticketing-user-request", requestId, req);

        // 요청 전송 및 응답 대기 (5 타임아웃)
        try {
            RequestReplyFuture<String, UserRequest, UserReply> future = kafkaTemplate.sendAndReceive(record);
            ConsumerRecord<String, UserReply> replyRecord = future.get(5, TimeUnit.SECONDS);
            return replyRecord.value();
        } catch (Exception e) {
            log.error("[fetchUserId] {}", e.getMessage());
            throw new UserException(UserErrorCode.USER_VALIDATION_FAILED);
        }
    }
}
