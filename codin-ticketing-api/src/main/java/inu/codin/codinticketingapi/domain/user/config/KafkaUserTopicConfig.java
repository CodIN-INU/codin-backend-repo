package inu.codin.codinticketingapi.domain.user.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaUserTopicConfig {

    @Bean
    public NewTopic ticketingUserReplyTopic() {
        return TopicBuilder.name("ticketing-user-reply")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ticketingUserRequestTopic() {
        return TopicBuilder.name("ticketing-user-request")
                .partitions(1)
                .replicas(1)
                .build();
    }
}

