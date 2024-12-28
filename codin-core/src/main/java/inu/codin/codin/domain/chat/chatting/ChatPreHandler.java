package inu.codin.codin.domain.chat.chatting;

import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.common.security.jwt.JwtTokenProvider;
import inu.codin.codin.domain.user.security.CustomUserDetailsService;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatPreHandler implements ChannelInterceptor {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private static final String BEARER_PREFIX="Bearer ";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel messageChannel){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (headerAccessor.getCommand() == StompCommand.CONNECT) {
            // 헤더 토큰 얻기
            String authorizationHeader = String.valueOf(headerAccessor.getNativeHeader("Authorization"));
            if (authorizationHeader == null || authorizationHeader.equals("null")) {
                throw new MalformedJwtException("[Chatting] JWT를 찾을 수 없습니다.");
            }
            String token = authorizationHeader.substring(BEARER_PREFIX.length());

            // 토큰 인증
            try {
                if (jwtTokenProvider.validateAccessToken(token)) {
                    String email = jwtTokenProvider.getUsername(token);
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                    headerAccessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
                }
            } catch (MessageDeliveryException e) {
                throw new MessageDeliveryException("[Chatting] 메세지 에러");
            } catch (MalformedJwtException e) {
                throw new MalformedJwtException("[Chatting] JWT 오류");
            } catch (Exception e) {
                throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "[Chatting] JWT 오류");
            }
        }

        return message;
    }
}
