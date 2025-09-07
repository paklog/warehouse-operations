package com.paklog.warehouse.domain.workload;

import com.paklog.warehouse.domain.shared.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Workload Orchestrator Tests")
class WorkloadOrchestratorTest {

    private WorkloadOrchestrator orchestrator;

    @Mock
    private IWorkloadReleaseStrategy mockStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orchestrator = new WorkloadOrchestrator(mockStrategy);
    }

    @Nested
    @DisplayName("Workload Release")
    class WorkloadRelease {
        @Test
        @DisplayName("Should release workload for valid orders")
        void shouldReleaseWorkloadForValidOrders() {
            // Arrange
            FulfillmentOrder order1 = createFulfillmentOrder("standard", "SKU-001", 2);
            FulfillmentOrder order2 = createFulfillmentOrder("express", "SKU-002", 3);
            
            WorkloadReleaseContext expectedContext = new WorkloadReleaseContext(List.of(order1, order2));
            when(mockStrategy.planWork(List.of(order1, order2))).thenReturn(new WorkloadPlan());

            // Act
            orchestrator.handleFulfillmentOrderValidated(order1);
            orchestrator.handleFulfillmentOrderValidated(order2);

            // Assert
            verify(mockStrategy).planWork(List.of(order1, order2));
        }

        @Test
        @DisplayName("Should throw exception for null order")
        void shouldThrowExceptionForNullOrder() {
            // Act & Assert
            assertThatThrownBy(() -> orchestrator.handleFulfillmentOrderValidated(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fulfillment order cannot be null");
        }
    }

    @Nested
    @DisplayName("Domain Events")
    class DomainEvents {
        @Test
        @DisplayName("Should generate domain events for workload release")
        void shouldGenerateDomainEventsForWorkloadRelease() {
            // Arrange
            FulfillmentOrder order1 = createFulfillmentOrder("standard", "SKU-001", 2);
            FulfillmentOrder order2 = createFulfillmentOrder("express", "SKU-002", 3);
            
            WorkloadPlan mockPlan = mock(WorkloadPlan.class);
            when(mockStrategy.planWork(List.of(order1, order2))).thenReturn(mockPlan);
            
            List<DomainEvent> expectedEvents = List.of(
                new PickListCreatedEvent(),
                new PickListAssignedEvent()
            );
            when(mockPlan.getEvents()).thenReturn(expectedEvents);

            // Act
            orchestrator.handleFulfillmentOrderValidated(order1);
            orchestrator.handleFulfillmentOrderValidated(order2);

            // Assert
            List<DomainEvent> actualEvents = orchestrator.getDomainEvents();
            assertThat(actualEvents).hasSize(2);
            assertThat(actualEvents).containsExactlyElementsOf(expectedEvents);
        }

        @Test
        @DisplayName("Should clear domain events after retrieval")
        void shouldClearDomainEventsAfterRetrieval() {
            // Arrange
            FulfillmentOrder order = createFulfillmentOrder("standard", "SKU-001", 2);
            
            WorkloadPlan mockPlan = mock(WorkloadPlan.class);
            when(mockStrategy.planWork(List.of(order))).thenReturn(mockPlan);
            
            List<DomainEvent> expectedEvents = List.of(
                new PickListCreatedEvent(),
                new PickListAssignedEvent()
            );
            when(mockPlan.getEvents()).thenReturn(expectedEvents);

            // Act
            orchestrator.handleFulfillmentOrderValidated(order);
            List<DomainEvent> firstRetrieval = orchestrator.getDomainEvents();
            List<DomainEvent> secondRetrieval = orchestrator.getDomainEvents();

            // Assert
            assertThat(firstRetrieval).hasSize(2);
            assertThat(secondRetrieval).isEmpty();
        }
    }

    @Nested
    @DisplayName("Strategy Interaction")
    class StrategyInteraction {
        @Test
        @DisplayName("Should use configured workload release strategy")
        void shouldUseConfiguredWorkloadReleaseStrategy() {
            // Arrange
            FulfillmentOrder order1 = createFulfillmentOrder("standard", "SKU-001", 2);
            FulfillmentOrder order2 = createFulfillmentOrder("express", "SKU-002", 3);
            
            WorkloadPlan expectedPlan = new WorkloadPlan();
            when(mockStrategy.planWork(List.of(order1, order2))).thenReturn(expectedPlan);

            // Act
            orchestrator.handleFulfillmentOrderValidated(order1);
            orchestrator.handleFulfillmentOrderValidated(order2);

            // Assert
            verify(mockStrategy).planWork(List.of(order1, order2));
        }
    }

    // Helper method to create fulfillment orders
    private FulfillmentOrder createFulfillmentOrder(String orderType, String skuCode, int quantity) {
        return new FulfillmentOrder(
            OrderId.generate(), 
            orderType, 
            new Address(), 
            List.of(new OrderItem(SkuCode.of(skuCode), Quantity.of(quantity)))
        );
    }
}