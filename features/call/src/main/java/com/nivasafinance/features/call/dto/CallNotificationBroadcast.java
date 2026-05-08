package com.nivasafinance.features.call.dto;

import com.nivasafinance.common.dto.CallNotificationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper DTO for broadcasting call notifications via Redis Pub/Sub.
 * Contains the notification payload, the list of target usernames,
 * and the source instance ID to prevent duplicate delivery on the publishing instance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallNotificationBroadcast {
    private CallNotificationResponse notification;
    private List<String> usernames;
    private String sourceInstanceId;
}
