package com.nivasafinance.common.messaging.publisher.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.messaging.config.MessagingProperties;
import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.messaging.publisher.MessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(SqsClient.class)
@Slf4j
public class SqsMessagePublisher implements MessagePublisher {

    private final SqsClient sqsClient;
    private final MessagingProperties messagingProperties;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(QueueType queueType, String messageId, Map<String, Object> payload) {
        String queueUrl = messagingProperties.getSqs().resolveQueueUrl(queueType);

        Map<String, Object> message = new HashMap<>(payload);
        message.putIfAbsent("messageId", messageId);

        try {
            String body = objectMapper.writeValueAsString(message);
            SendMessageRequest.Builder requestBuilder = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(body);

            if (queueUrl.endsWith(".fifo")) {
                requestBuilder = requestBuilder
                        .messageGroupId(queueType.name())
                        .messageDeduplicationId(messageId);
            }

            sqsClient.sendMessage(requestBuilder.build());
            log.info("Published message to SQS queue {} with id {}", queueUrl, messageId);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize message payload for queue " + queueUrl, e);
        }
    }
}
