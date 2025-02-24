package inu.codin.codin.common.stomp;

import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.common.security.service.JwtService;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@RequiredArgsConstructor
public class HttpHandShakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest){
            ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;

            try {
                jwtService.setAuthentication(serverHttpRequest);
            } catch (MessageDeliveryException e) {
                throw new MessageDeliveryException("[Chatting] Jwt로 인한 메세지 전송 오류입니다.");
            } catch (MalformedJwtException e) {
                throw new MalformedJwtException("[Chatting] 비정상적인 jwt 토큰 입니다.");
            } catch (Exception e) {
                throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "[Chatting] 인증되지 않은 jwt 토큰 입니다.");
            }
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }


}
