package inu.codin.codin.infra.redis.service;

import inu.codin.codin.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisChatService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final String CHATROOM_KEY = "chat:participant_counter:";

    public void enterToChatroom(String chatRoomId){
        String counterKey = CHATROOM_KEY + chatRoomId;
        Object counterAnonNum = redisTemplate.opsForValue().get(counterKey);
        if (counterAnonNum == null){
            redisTemplate.opsForValue().set(counterKey, 1); //카운터 1부터 시작
        }
        redisTemplate.opsForValue().increment(counterKey);
    }

    public void exitToChatroom(String chatRoomId){
        String counterKey = CHATROOM_KEY + chatRoomId;
        Object counterAnonNum = redisTemplate.opsForValue().get(counterKey);

        if (counterAnonNum != null)
            if (counterAnonNum.toString().equals("0"))
                log.error("[RedisChatService] 채팅방을 나가지만 참여자 수가 0 이라 음수가 됩니다.");
            else redisTemplate.opsForValue().set(counterKey, Integer.parseInt(counterAnonNum.toString())-1);
        else {
            log.error("[RedisChatService] 해당 chatroom의 참여자 수를 가져올 수 없습니다. chatRoomId : " + chatRoomId);
            throw new NotFoundException("[RedisChatService] 해당 chatroom의 참여자 수를 가져올 수 없습니다.");
        }
    }

    public Integer countOfChatRoomParticipant(String chatRoomId) {
        String counterKey = CHATROOM_KEY + chatRoomId;
        Object counterAnonNum = redisTemplate.opsForValue().get(counterKey);

        if (counterAnonNum != null)
            return Integer.parseInt(counterAnonNum.toString());
        else {
            log.error("[RedisChatService] 해당 chatroom의 참여자 수를 가져올 수 없습니다. chatRoomId : " + chatRoomId);
            throw new NotFoundException("[RedisChatService] 해당 chatroom의 참여자 수를 가져올 수 없습니다.");
        }
    }

}
