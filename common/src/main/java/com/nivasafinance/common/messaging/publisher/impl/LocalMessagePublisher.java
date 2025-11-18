package com.nivasafinance.common.messaging.publisher.impl;

import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.messaging.publisher.MessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class LocalMessagePublisher implements MessagePublisher {

    @Override
    public void publish(QueueType queueType, String messageId, Map<String, Object> payload) {
        log.info("[LOCAL QUEUE] type={}, messageId={}, payload={}", queueType, messageId, payload);
    }
}


