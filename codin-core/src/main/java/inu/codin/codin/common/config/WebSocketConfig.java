package inu.codin.codin.common.config;

import inu.codin.codin.common.security.service.JwtService;
import inu.codin.codin.common.stomp.HttpHandShakeInterceptor;
import inu.codin.codin.common.stomp.StompMessageProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${server.domain}")
    private String BASEURL;

    private final StompMessageProcessor stompMessageProcessor;
    private final JwtService jwtService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp") //handshake endpoint
                .setAllowedOriginPatterns("http://localhost:3000", "http://localhost:8080", BASEURL)
                .withSockJS()
                .setInterceptors(new HttpHandShakeInterceptor(jwtService));
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        //해당 주소를 구독 및 구독하고 있는 클라이언트들에게 메세지 전달
        //메세지를 브로커로 라우팅
        registry.setApplicationDestinationPrefixes("/pub");
        //클라이언트에서 보낸 메세지를 받을 prefix, controller의 @MessageMapping과 이어짐
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(50 * 1024 * 1024); // 메세지 크기 제한 오류 방지(이 코드가 없으면 byte code를 보낼때 소켓 연결이 끊길 수 있음)
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompMessageProcessor);
    }
}
