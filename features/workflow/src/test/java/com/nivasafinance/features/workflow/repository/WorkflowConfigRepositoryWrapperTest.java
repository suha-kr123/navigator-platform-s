package com.nivasafinance.features.workflow.repository;

import com.nivasafinance.features.workflow.entity.WorkflowConfig;
import com.nivasafinance.features.workflow.exception.WorkflowConfigNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowConfigRepositoryWrapperTest {

    @Mock
    private WorkflowConfigRepository repository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private WorkflowConfigRepositoryWrapper wrapper;

    // ── findActiveByWorkflowConfigKey ──

    @Test
    void findActiveByWorkflowConfigKey_whenFound_returnsConfig() {
        WorkflowConfig config = new WorkflowConfig();
        config.setWorkflowConfigKey("WF_KEY");
        when(repository.findByWorkflowConfigKey("WF_KEY")).thenReturn(Optional.of(config));

        WorkflowConfig result = wrapper.findActiveByWorkflowConfigKey("WF_KEY");

        assertEquals("WF_KEY", result.getWorkflowConfigKey(),
                "Should return the workflow config matching the key");
    }

    @Test
    void findActiveByWorkflowConfigKey_whenNotFound_throwsWorkflowConfigNotFoundException() {
        when(repository.findByWorkflowConfigKey("MISSING")).thenReturn(Optional.empty());

        assertThrows(WorkflowConfigNotFoundException.class,
                () -> wrapper.findActiveByWorkflowConfigKey("MISSING"),
                "Should throw WorkflowConfigNotFoundException when config key not found");
    }
}
