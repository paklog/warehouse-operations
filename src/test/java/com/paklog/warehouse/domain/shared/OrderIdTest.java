package com.paklog.warehouse.domain.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderId Domain Tests")
class OrderIdTest {

    @Nested
    @DisplayName("OrderId Creation")
    class OrderIdCreation {

        @Test
        @DisplayName("Should generate a valid OrderId")
        void shouldGenerateValidOrderId() {
            // Act
            OrderId orderId = OrderId.generate();

            // Assert
            assertThat(orderId).isNotNull();
            assertThat(orderId.getValue()).isNotNull();
            assertThat(orderId.toString()).isNotNull();
        }

        @Test
        @DisplayName("Should create OrderId from valid UUID string")
        void shouldCreateOrderIdFromValidUuidString() {
            // Arrange
            String uuidString = "123e4567-e89b-12d3-a456-426614174000";

            // Act
            OrderId orderId = OrderId.of(uuidString);

            // Assert
            assertThat(orderId).isNotNull();
            assertThat(orderId.getValue()).isEqualTo(UUID.fromString(uuidString));
            assertThat(orderId.toString()).isEqualTo(uuidString);
        }

        @Test
        @DisplayName("Should throw exception for invalid UUID string")
        void shouldThrowExceptionForInvalidUuidString() {
            // Arrange
            String invalidUuid = "invalid-uuid";

            // Act & Assert
            assertThatThrownBy(() -> OrderId.of(invalidUuid))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception for null UUID")
        void shouldThrowExceptionForNullUuid() {
            // Act & Assert
            assertThatThrownBy(() -> OrderId.of((String) null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("OrderId Equality")
    class OrderIdEquality {

        @Test
        @DisplayName("Should consider equal OrderIds with same UUID")
        void shouldConsiderEqualOrderIdsWithSameUuid() {
            // Arrange
            String uuidString = "123e4567-e89b-12d3-a456-426614174000";
            OrderId orderId1 = OrderId.of(uuidString);
            OrderId orderId2 = OrderId.of(uuidString);

            // Assert
            assertThat(orderId1).isEqualTo(orderId2);
            assertThat(orderId1.hashCode()).isEqualTo(orderId2.hashCode());
        }

        @Test
        @DisplayName("Should consider different OrderIds with different UUIDs")
        void shouldConsiderDifferentOrderIdsWithDifferentUuids() {
            // Arrange
            OrderId orderId1 = OrderId.generate();
            OrderId orderId2 = OrderId.generate();

            // Assert
            assertThat(orderId1).isNotEqualTo(orderId2);
        }

        @Test
        @DisplayName("Should handle null comparison")
        void shouldHandleNullComparison() {
            // Arrange
            OrderId orderId = OrderId.generate();

            // Assert
            assertThat(orderId).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should handle different class comparison")
        void shouldHandleDifferentClassComparison() {
            // Arrange
            OrderId orderId = OrderId.generate();
            String notOrderId = "not an order id";

            // Assert
            assertThat(orderId).isNotEqualTo(notOrderId);
        }
    }

    @Nested
    @DisplayName("OrderId String Representation")
    class OrderIdStringRepresentation {

        @Test
        @DisplayName("Should provide correct string representation")
        void shouldProvideCorrectStringRepresentation() {
            // Arrange
            String uuidString = "123e4567-e89b-12d3-a456-426614174000";
            OrderId orderId = OrderId.of(uuidString);

            // Act & Assert
            assertThat(orderId.toString()).isEqualTo(uuidString);
        }
    }
}