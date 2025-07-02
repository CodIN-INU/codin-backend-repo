package inu.codin.codin.common.exception;

import inu.codin.codin.common.response.ExceptionResponse;
import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.domain.chat.chatroom.exception.ChatRoomExistedException;
import inu.codin.codin.domain.chat.exception.*;
import inu.codin.codin.domain.info.exception.InfoErrorCode;
import inu.codin.codin.domain.info.exception.InfoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ExceptionResponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(GlobalException.class)
    protected ResponseEntity<ExceptionResponse> handleGlobalException(GlobalException e) {
        GlobalErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(code.message(), code.httpStatus().value()));
    }

    @ExceptionHandler(ChatRoomException.class)
    protected ResponseEntity<ExceptionResponse> handleChatRoomException(ChatRoomException e) {
        ChatRoomErrorCode code = e.getErrorCode();
        String message = code.message();
//        if (e instanceof ChatRoomExistedException existedException) //client 측에서 303 상태 코드 확인 후 message의 chatRoomId로 리다이렉션
//            message = message + "/" + existedException.getChatRoomId();
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(message, code.httpStatus().value()));
    }

    @ExceptionHandler(ChattingException.class)
    protected ResponseEntity<ExceptionResponse> handleChattingException(ChattingException e) {
        ChattingErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(code.message(), code.httpStatus().value()));
    }

    @ExceptionHandler(InfoException.class)
    protected ResponseEntity<ExceptionResponse> handleInfoException(InfoException e) {
        InfoErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(new ExceptionResponse(code.message(), code.httpStatus().value()));
    }

    @ExceptionHandler(NotFoundException.class)
    protected ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(JwtException.class)
    protected ResponseEntity<ExceptionResponse> handleJwtException(JwtException e) {
        if (e.getErrorCode().getErrorCode().equals("SEC_005")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ExceptionResponse(e.getMessage(), HttpStatus.FORBIDDEN.value()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ExceptionResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getBindingResult().getFieldErrors().get(0).getDefaultMessage(), HttpStatus.BAD_REQUEST.value()));    }



    @ExceptionHandler(RedisSystemException.class)
    public ResponseEntity<ExceptionResponse> handleRedisSystemException(RedisSystemException e){
        log.error(e.getMessage(), e.getStackTrace()[0]);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(OAuth2AuthenticationException.class)
    protected ResponseEntity<ExceptionResponse> handleOAuth2AuthenticationException(OAuth2AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionResponse(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

}
