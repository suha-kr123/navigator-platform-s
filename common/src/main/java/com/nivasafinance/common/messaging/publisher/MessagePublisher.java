package com.nivasafinance.common.messaging.publisher;

import com.nivasafinance.common.messaging.enums.QueueType;

import java.util.Map;

public interface MessagePublisher {

    void publish(QueueType queueType, String messageId, Map<String, Object> payload);
}


