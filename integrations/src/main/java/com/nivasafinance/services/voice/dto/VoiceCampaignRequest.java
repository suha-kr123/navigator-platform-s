package com.nivasafinance.services.voice.dto;

import com.nivasafinance.common.enums.SystemEntities;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


// Either send listId or file depending on the provider
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCampaignRequest {
    private String name;
    private String listId;
    private String callerId;
    private String callFlowId;
    private Integer noOfRetries;
    private Integer retryIntervalMins;
    private Integer cpm; // Calls per minute (throttle)
    private MultipartFile file;
    private CallBackData callBackData;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class CallBackData{
        private SystemEntities entityType;
        private String identifier;
    }
}
