package inu.codin.codin.common.util;

import org.bson.types.ObjectId;

public class ObjectIdUtil {
    private ObjectIdUtil() {}

    /**
     * String을 ObjectId로 변환
     * @param objectIdString ObjectId 문자열
     * @return ObjectId 객체
     * @throws IllegalArgumentException 유효하지 않은 ObjectId 형식인 경우
     */
    public static ObjectId toObjectId(String objectIdString) {
        try {
            return new ObjectId(objectIdString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 ObjectId 형식입니다: " + objectIdString, e);
        }
    }

    /**
     * ObjectId를 String으로 변환
     * @param objectId ObjectId 객체
     * @return ObjectId 문자열
     */
    public static String toString(ObjectId objectId) {
        return objectId != null ? objectId.toHexString() : null;
    }

    /**
     * ObjectId 유효성 검증
     * @param objectIdString 검증할 문자열
     * @return 유효한 ObjectId 형식이면 true
     */
    public static boolean isValid(String objectIdString) {
        return ObjectId.isValid(objectIdString);
    }
}
