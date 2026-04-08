package com.nivasafinance.features.advisoractivity.listener;

import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.features.advisoractivity.factory.AdvisorActivityDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorActivityListenerTest {

    @Mock
    private AdvisorActivityDataFactory advisorActivityDataFactory;

    @InjectMocks
    private AdvisorActivityListener advisorActivityListener;

    @Test
    void handleEvent_delegatesToFactory() {
        SystemEvent<String> event = new SystemEvent<>("EVT", "payload", "user1");
        advisorActivityListener.handleEvent(event);
        verify(advisorActivityDataFactory).recordEvent("EVT", "payload", "user1");
    }
}
