package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.task.LeadTaskTimelineComputer;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.service.TaskReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadTaskTimelineService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final TaskReadService taskReadService;

    @Transactional
    public void refreshByLeadIdentifier(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        List<TaskResponse> tasks = taskReadService.findAllTasksForLead(leadIdentifier, true);
        Lead.TaskTimeline timeline = LeadTaskTimelineComputer.compute(tasks);
        lead.setTaskTimeline(timeline);
        leadRepositoryWrapper.saveWithException(lead);
    }
}
