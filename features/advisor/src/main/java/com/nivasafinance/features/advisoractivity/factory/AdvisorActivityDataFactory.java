package com.nivasafinance.features.advisoractivity.factory;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.payload.*;
import com.nivasafinance.features.advisoractivity.dto.CreateAdvisorActivityRequest;
import com.nivasafinance.features.advisoractivity.enums.ResourceAction;
import com.nivasafinance.features.advisoractivity.enums.ResourceEnum;
import com.nivasafinance.features.advisoractivity.service.AdvisorActivityWriteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class AdvisorActivityDataFactory {
    private final AdvisorActivityWriteService writeService;

    public <T> void recordEvent(String eventType, T payload) {
        BusinessEvent event = BusinessEvent.valueOf(eventType);

        switch (event) {
            case ADVISOR_CREATED -> {
                CreateAdvisorActivityRequest request = createAdvisorCreatedActivityRequest((AdvisorCreationEventPayload) payload);
                writeService.createAdvisorActivity(request);
            }
            case ADVISOR_UPDATED -> {
                CreateAdvisorActivityRequest request = createAdvisorUpdatedActivityRequest((AdvisorUpdateEventPayload) payload);
                writeService.createAdvisorActivity(request);
            }
            case ADVISOR_NOTE_CREATED -> {
                CreateAdvisorActivityRequest request = createAdvisorNoteCreatedActivityRequest((AdvisorNoteCreationEventPayload) payload);
                writeService.createAdvisorActivity(request);
            }
            case ADVISOR_NOTE_UPDATED -> {
                CreateAdvisorActivityRequest request = createAdvisorNoteUpdatedActivityRequest((AdvisorNoteUpdationEventPayload) payload);
                writeService.createAdvisorActivity(request);
            }
            case ADVISOR_NOTE_DELETED -> {
                CreateAdvisorActivityRequest request = createAdvisorNoteDeletedActivityRequest((AdvisorNoteDeletionEventPayload) payload);
                writeService.createAdvisorActivity(request);
            }
            case ADVISOR_CALL_LOG_CREATED -> {
                CreateAdvisorActivityRequest request = createAdvisorCallLogCreatedActivityRequest((AdvisorCallLogCreationEventPayload) payload);
                writeService.createAdvisorActivity(request);
            }
            case ADVISOR_CALL_LOG_UPDATED -> {
                CreateAdvisorActivityRequest request = createAdvisorCallLogUpdatedActivityRequest((AdvisorCallLogUpdateEventPayload) payload);
                writeService.createAdvisorActivity(request);
            }
            case ADVISOR_REJECTED, ADVISOR_DORMANT, ADVISOR_ACTIVE -> {
                CreateAdvisorActivityRequest request = createAdvisorStatusChangeActivityRequest(event, (AdvisorStatusChangeEventPayload) payload);
                writeService.createAdvisorActivity(request);
            }
            default -> {
                // Not supported
            }
        }
    }

    private CreateAdvisorActivityRequest createAdvisorCreatedActivityRequest(AdvisorCreationEventPayload payload) {
        return CreateAdvisorActivityRequest.builder()
                .advisorId(payload.getId())
                .resourceId(payload.getId())
                .description("Advisor created by " + UserContext.getUsername())
                .resource(ResourceEnum.ADVISOR)
                .action(ResourceAction.CREATE)
                .build();
    }

    private CreateAdvisorActivityRequest createAdvisorUpdatedActivityRequest(AdvisorUpdateEventPayload payload) {
        return CreateAdvisorActivityRequest.builder()
                .advisorId(payload.getId())
                .resourceId(payload.getId())
                .description("Advisor updated by " + UserContext.getUsername())
                .resource(ResourceEnum.ADVISOR)
                .action(ResourceAction.UPDATE)
                .build();
    }

    private CreateAdvisorActivityRequest createAdvisorNoteCreatedActivityRequest(AdvisorNoteCreationEventPayload payload) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("noteIdentifier", payload.getNoteIdentifier().toString());

        return CreateAdvisorActivityRequest.builder()
                .advisorId(payload.getAdvisorId())
                .resourceId(payload.getNoteId())
                .description("Note created by " + UserContext.getUsername())
                .resource(ResourceEnum.NOTES)
                .action(ResourceAction.CREATE)
                .metadata(metadata)
                .build();
    }

    private CreateAdvisorActivityRequest createAdvisorNoteUpdatedActivityRequest(AdvisorNoteUpdationEventPayload payload) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("noteIdentifier", payload.getNoteIdentifier().toString());

        return CreateAdvisorActivityRequest.builder()
                .advisorId(payload.getAdvisorId())
                .resourceId(payload.getNoteId())
                .description("Note updated by " + UserContext.getUsername())
                .resource(ResourceEnum.NOTES)
                .action(ResourceAction.UPDATE)
                .metadata(metadata)
                .build();
    }

    private CreateAdvisorActivityRequest createAdvisorNoteDeletedActivityRequest(AdvisorNoteDeletionEventPayload payload) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("noteIdentifier", payload.getNoteIdentifier().toString());

        return CreateAdvisorActivityRequest.builder()
                .advisorId(payload.getAdvisorId())
                .resourceId(payload.getNoteId())
                .description("Note deleted by " + UserContext.getUsername())
                .resource(ResourceEnum.NOTES)
                .action(ResourceAction.DELETE)
                .metadata(metadata)
                .build();
    }

    private CreateAdvisorActivityRequest createAdvisorCallLogCreatedActivityRequest(AdvisorCallLogCreationEventPayload payload) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("callLogIdentifier", payload.getCallLogIdentifier().toString());

        return CreateAdvisorActivityRequest.builder()
                .advisorId(payload.getAdvisorId())
                .resourceId(payload.getCallLogId())
                .description("Call log created by " + UserContext.getUsername())
                .resource(ResourceEnum.CALL_LOG)
                .action(ResourceAction.CREATE)
                .metadata(metadata)
                .build();
    }

    private CreateAdvisorActivityRequest createAdvisorCallLogUpdatedActivityRequest(AdvisorCallLogUpdateEventPayload payload){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("callLogIdentifier", payload.getCallLogIdentifier().toString());

        return CreateAdvisorActivityRequest.builder()
                .advisorId(payload.getAdvisorId())
                .resourceId(payload.getCallLogId())
                .description("Call log updated by " + UserContext.getUsername())
                .resource(ResourceEnum.CALL_LOG)
                .action(ResourceAction.UPDATE)
                .metadata(metadata)
                .build();
    }

    private CreateAdvisorActivityRequest createAdvisorStatusChangeActivityRequest(BusinessEvent event, AdvisorStatusChangeEventPayload payload) {
        Map<String, Object> metadata = new HashMap<>();
        if (payload.getReason() != null) {
            metadata.put("reason", payload.getReason());
        }

        String description = getStatusChangeDescription(event);

        return CreateAdvisorActivityRequest.builder()
                .advisorId(payload.getId())
                .resourceId(payload.getId())
                .description(description)
                .resource(ResourceEnum.ADVISOR)
                .action(ResourceAction.STATUS_CHANGE)
                .metadata(metadata)
                .build();
    }

    private String getStatusChangeDescription(BusinessEvent event) {
        String action = switch (event) {
            case ADVISOR_REJECTED -> "rejected";
            case ADVISOR_DORMANT -> "put on dormant";
            case ADVISOR_ACTIVE -> "activated";
            default -> "status changed";
        };
        return "Advisor " + action + " by " + UserContext.getUsername();
    }
}

