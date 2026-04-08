package com.nivasafinance.features.workflow.adapter;

import com.nivasafinance.common.enums.EntityType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntityWorkflowAdapterRegistryTest {

    // ── getAdapter ──

    @Test
    void getAdapter_whenAdapterExists_returnsAdapter() {
        EntityWorkflowAdapter leadAdapter = mock(EntityWorkflowAdapter.class);
        when(leadAdapter.getEntityType()).thenReturn(EntityType.LEAD);

        EntityWorkflowAdapterRegistry registry = new EntityWorkflowAdapterRegistry(List.of(leadAdapter));

        EntityWorkflowAdapter result = registry.getAdapter(EntityType.LEAD);

        assertSame(leadAdapter, result, "Should return the adapter registered for LEAD entity type");
    }

    @Test
    void getAdapter_whenMultipleAdapters_returnsCorrectOne() {
        EntityWorkflowAdapter leadAdapter = mock(EntityWorkflowAdapter.class);
        when(leadAdapter.getEntityType()).thenReturn(EntityType.LEAD);
        EntityWorkflowAdapter advisorAdapter = mock(EntityWorkflowAdapter.class);
        when(advisorAdapter.getEntityType()).thenReturn(EntityType.ADVISOR);

        EntityWorkflowAdapterRegistry registry = new EntityWorkflowAdapterRegistry(
                List.of(leadAdapter, advisorAdapter));

        assertSame(advisorAdapter, registry.getAdapter(EntityType.ADVISOR),
                "Should return the ADVISOR adapter when requested");
        assertSame(leadAdapter, registry.getAdapter(EntityType.LEAD),
                "Should return the LEAD adapter when requested");
    }

    @Test
    void getAdapter_whenNoAdapterForType_throwsIllegalArgumentException() {
        EntityWorkflowAdapter leadAdapter = mock(EntityWorkflowAdapter.class);
        when(leadAdapter.getEntityType()).thenReturn(EntityType.LEAD);

        EntityWorkflowAdapterRegistry registry = new EntityWorkflowAdapterRegistry(List.of(leadAdapter));

        assertThrows(IllegalArgumentException.class,
                () -> registry.getAdapter(EntityType.ADVISOR),
                "Should throw IllegalArgumentException when no adapter registered for entity type");
    }

    @Test
    void getAdapter_lazyInitializesMapOnFirstCall() {
        EntityWorkflowAdapter adapter = mock(EntityWorkflowAdapter.class);
        when(adapter.getEntityType()).thenReturn(EntityType.LEAD);

        EntityWorkflowAdapterRegistry registry = new EntityWorkflowAdapterRegistry(List.of(adapter));

        EntityWorkflowAdapter first = registry.getAdapter(EntityType.LEAD);
        EntityWorkflowAdapter second = registry.getAdapter(EntityType.LEAD);

        assertSame(first, second, "Subsequent calls should return the same adapter from cached map");
        verify(adapter, times(1)).getEntityType();
    }

    @Test
    void getAdapter_withEmptyAdapterList_throwsIllegalArgumentException() {
        EntityWorkflowAdapterRegistry registry = new EntityWorkflowAdapterRegistry(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> registry.getAdapter(EntityType.LEAD),
                "Should throw IllegalArgumentException when no adapters are registered");
    }
}
