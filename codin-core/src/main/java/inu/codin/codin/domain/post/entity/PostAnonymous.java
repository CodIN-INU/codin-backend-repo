package inu.codin.codin.domain.post.entity;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class PostAnonymous {
    private Map<String, Integer> userAnonymousMap = new HashMap<>();
    private int anonymousNumber = 1;

    /**
     * Map을 통해 유저의 익명 번호 설정 및 반환
     * @param userId 유저 _id
     * @return 게시글에서 유저의 익명 번호
     */
    public Integer getAnonNumber(String userId){
        if (userAnonymousMap.containsKey(userId))
            return userAnonymousMap.get(userId);
        else {
            userAnonymousMap.put(userId, anonymousNumber);
            return anonymousNumber++;
        }
    }

    /**
     * 글쓴이는 따로 관리하기 위해 0으로 설정
     * @param userId
     */
    public void setWriter(String userId){
        userAnonymousMap.put(userId, 0);
    }




}
