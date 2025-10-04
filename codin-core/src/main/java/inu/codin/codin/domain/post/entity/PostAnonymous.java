package inu.codin.codin.domain.post.entity;

import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

@Getter
public class PostAnonymous {
    private Map<String, Integer> userAnonymousMap = new HashMap<>();
    private int anonymousNumber = 1;

    /**
     * Map을 통해 유저의 익명 번호 반환 (없으면 null)
     * @param userId 유저 _id
     * @return 게시글에서 유저의 익명 번호
     */
    public Integer getAnonNumber(ObjectId userId) {
        return userAnonymousMap.get(userId.toString());
    }

    /**
     * 글쓴이는 따로 관리하기 위해 0으로 설정
     * @param userId
     */
    public void setWriter(ObjectId userId){
        userAnonymousMap.put(userId.toString(), 0);
    }

    /**
     * 유저에게 익명 번호 할당
     */
    public void setAnonNumber(ObjectId userId) {
        userAnonymousMap.put(userId.toString(), this.anonymousNumber++);
    }

    /**
     * 해당 유저가 이미 익명 번호를 받았는지 확인
     */
    public boolean hasAnonNumber(ObjectId userId) {
        return userAnonymousMap.containsKey(userId.toString());
    }
}
