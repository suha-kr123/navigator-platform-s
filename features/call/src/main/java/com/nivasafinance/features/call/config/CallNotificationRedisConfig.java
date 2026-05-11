package com.nivasafinance.features.call.config;

import com.nivasafinance.features.call.service.CallNotificationRedisSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@Slf4j
public class CallNotificationRedisConfig {

    public static final String CALL_NOTIFICATION_CHANNEL = "call:notification:broadcast";

    @Bean
    public ChannelTopic callNotificationTopic() {
        return new ChannelTopic(CALL_NOTIFICATION_CHANNEL);
    }

    @Bean
    public RedisMessageListenerContainer callNotificationListenerContainer(
            RedisConnectionFactory connectionFactory,
            CallNotificationRedisSubscriber subscriber,
            ChannelTopic callNotificationTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // Register subscriber directly — it implements MessageListener, no adapter needed
        container.addMessageListener(subscriber, callNotificationTopic);
        container.setErrorHandler(t -> log.error("Error in call notification Redis listener", t));
        return container;
    }
}
