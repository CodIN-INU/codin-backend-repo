package inu.codin.codin.common;

import inu.codin.codin.common.security.exception.SecurityErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public class ResponseUtils {

    /**
     * 성공 응답
     * @param data class 형태의 데이터 타입 (e.g. DTO)
     * @return ResponseEntity<T>
     */
    public static ResponseEntity<?> success(Object data) {
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    /**
     * 성공 응답
     * @param message 성공 응답 메세지
     * @return ResponseEntity<?>
     */
    public static ResponseEntity<?> successMsg(String message) {
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    /**
     * 실패 응답
     * @param message 실패 응답 메세지 (404에러)
     * @return ResponseEntity<?>
     */
    public static ResponseEntity<?> error(String message) {
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    /**
     * 실패 응답
     * @param message 실패 응답 메세지
     * @param code SecurityErrorCode 참조
     * @return ResponseEntity<?>
     */
    public static ResponseEntity<?> error(String message, SecurityErrorCode code) {
        HashMap<String, Object> error = new HashMap<>();
        error.put("message", message);
        error.put("errorCode", code.getErrorCode());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
