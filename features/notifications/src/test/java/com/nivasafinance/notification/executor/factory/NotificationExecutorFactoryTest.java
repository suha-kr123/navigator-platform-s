package com.nivasafinance.notification.executor.factory;

import com.nivasafinance.notification.executor.NotificationExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationExecutorFactoryTest {

    private NotificationExecutorFactory factory;

    private NotificationExecutor watiWhatsAppExecutor;
    private NotificationExecutor firebaseAppExecutor;

    @BeforeEach
    void setUp() {
        watiWhatsAppExecutor = mock(NotificationExecutor.class);
        lenient().when(watiWhatsAppExecutor.getMode()).thenReturn("WATI");
        lenient().when(watiWhatsAppExecutor.getChannelType()).thenReturn("WHATSAPP");

        firebaseAppExecutor = mock(NotificationExecutor.class);
        lenient().when(firebaseAppExecutor.getMode()).thenReturn("FIREBASE");
        lenient().when(firebaseAppExecutor.getChannelType()).thenReturn("APP");

        factory = new NotificationExecutorFactory(List.of(watiWhatsAppExecutor, firebaseAppExecutor));
    }

    @Test
    void getExecutor_matchingModeAndChannel_returnsExecutor() {
        NotificationExecutor result = factory.getExecutor("WATI", "WHATSAPP");

        assertEquals(watiWhatsAppExecutor, result, "Should return WATI WHATSAPP executor");
    }

    @Test
    void getExecutor_firebaseModeAndAppChannel_returnsExecutor() {
        NotificationExecutor result = factory.getExecutor("FIREBASE", "APP");

        assertEquals(firebaseAppExecutor, result, "Should return FIREBASE APP executor");
    }

    @Test
    void getExecutor_noMatchingExecutor_throwsException() {
        assertThrows(IllegalStateException.class,
                () -> factory.getExecutor("TWILIO", "SMS"),
                "Should throw when no matching executor found");
    }

    @Test
    void getExecutor_lowercaseInput_normalizesToUppercase() {
        NotificationExecutor result = factory.getExecutor("wati", "whatsapp");

        assertEquals(watiWhatsAppExecutor, result, "Should match after case normalization");
    }

    @Test
    void getExecutor_mixedCaseInput_normalizesToUppercase() {
        NotificationExecutor result = factory.getExecutor("Wati", "WhatsApp");

        assertEquals(watiWhatsAppExecutor, result, "Should match after mixed case normalization");
    }

    @Test
    void getExecutor_nullMode_throwsException() {
        assertThrows(IllegalStateException.class,
                () -> factory.getExecutor(null, "WHATSAPP"),
                "Should throw for null mode");
    }

    @Test
    void getExecutor_nullChannelType_throwsException() {
        assertThrows(IllegalStateException.class,
                () -> factory.getExecutor("WATI", null),
                "Should throw for null channel type");
    }
}
