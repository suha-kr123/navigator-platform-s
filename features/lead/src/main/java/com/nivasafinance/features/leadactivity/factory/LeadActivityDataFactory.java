package com.nivasafinance.features.leadactivity.factory;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.payload.LeadCreationEventPayload;
import com.nivasafinance.features.leadactivity.dto.CreateLeadActivityRequest;
import com.nivasafinance.features.leadactivity.enums.ResourceAction;
import com.nivasafinance.features.leadactivity.enums.ResourceEnum;
import com.nivasafinance.features.leadactivity.service.LeadActivityWriteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LeadActivityDataFactory {
    private final LeadActivityWriteService writeService;

    public <T> void recordEvent(String eventType, T payload ){

        BusinessEvent event = BusinessEvent.valueOf(eventType);

        switch (event){
            case LEAD_CREATED -> {
                CreateLeadActivityRequest request = createLeadCreatedActivityRequest((LeadCreationEventPayload) payload);
                writeService.createLeadActivity(request);
            }
            default ->{
                //Not supported
            }
        }

    }

    private CreateLeadActivityRequest createLeadCreatedActivityRequest(LeadCreationEventPayload payload){
        return CreateLeadActivityRequest.builder()
                .leadId(payload.getId())
                .resourceId(payload.getId())
                .description("Lead created by " + UserContext.getUsername())
                .resource(ResourceEnum.LEAD)
                .action(ResourceAction.CREATE)
                .build();
    }

}
