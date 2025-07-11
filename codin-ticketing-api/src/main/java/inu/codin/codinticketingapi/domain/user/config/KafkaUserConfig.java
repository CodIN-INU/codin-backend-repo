package inu.codin.codinticketingapi.domain.user.config;

import inu.codin.codinticketingapi.domain.user.dto.UserReply;
import inu.codin.codinticketingapi.domain.user.dto.UserRequest;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
public class KafkaUserConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // 요청 전송용 ProducerFactory
    @Bean
    public ProducerFactory<String, UserRequest> userRequestProducerFactory() {
        Map<String, Object> config = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        );
        return new DefaultKafkaProducerFactory<>(config);
    }

    // 응답 리스닝용 ConsumerFactory
    @Bean
    public ConsumerFactory<String, UserReply> userReplyConsumerFactory() {
        Map<String, Object> config = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "ticketing-api-reply-group",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                JsonDeserializer.TRUSTED_PACKAGES, "inu.codin.codinticketingapi.domain.user.dto"
        );
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JsonDeserializer<>(UserReply.class, false)
        );
    }

    // Reply 컨테이너
    @Bean
    public ConcurrentMessageListenerContainer<String, UserReply> replyContainer(ConsumerFactory<String, UserReply> cf) {
        ContainerProperties cp = new ContainerProperties("ticketing-user-reply");
        return new ConcurrentMessageListenerContainer<>(cf, cp);
    }

    // Request–Reply 템플릿
    @Bean
    public ReplyingKafkaTemplate<String, UserRequest, UserReply> replyingKafkaTemplate(
            ProducerFactory<String, UserRequest> pf,
            ConcurrentMessageListenerContainer<String, UserReply> replyContainer
    ) {
        return new ReplyingKafkaTemplate<>(pf, replyContainer);
    }
}
