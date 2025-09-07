package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.PickInstruction;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Pick Route Optimizer Tests")
class PickRouteOptimizerTest {

    private PickRouteOptimizer optimizer;

    @BeforeEach
    void setUp() {
        optimizer = new PickRouteOptimizer();
    }

    @Nested
    @DisplayName("Route Optimization")
    class RouteOptimization {
        @Test
        @DisplayName("Should optimize pick route for single bin location")
        void shouldOptimizeRouteForSingleBinLocation() {
            // Arrange
            List<PickInstruction> instructions = new ArrayList<>();
            instructions.add(createPickInstruction("SKU-001", "A01", 2));

            // Act
            List<PickInstruction> optimizedRoute = optimizer.optimizeRoute(instructions);

            // Assert
            assertThat(optimizedRoute).hasSize(1);
            assertThat(optimizedRoute.get(0)).isEqualTo(instructions.get(0));
        }

        @Test
        @DisplayName("Should optimize route for multiple bin locations")
        void shouldOptimizeRouteForMultipleBinLocations() {
            // Arrange
            List<PickInstruction> instructions = new ArrayList<>();
            instructions.add(createPickInstruction("SKU-001", "A01", 2));
            instructions.add(createPickInstruction("SKU-002", "B02", 3));
            instructions.add(createPickInstruction("SKU-003", "A01", 1));

            // Act
            List<PickInstruction> optimizedRoute = optimizer.optimizeRoute(instructions);

            // Assert
            assertThat(optimizedRoute).hasSize(3);
            // Check that instructions with same bin location are grouped together
            assertThat(optimizedRoute.get(0).getBinLocation().getValue()).isEqualTo("A01");
            assertThat(optimizedRoute.get(1).getBinLocation().getValue()).isEqualTo("A01");
            assertThat(optimizedRoute.get(2).getBinLocation().getValue()).isEqualTo("B02");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        @Test
        @DisplayName("Should handle empty pick instructions")
        void shouldHandleEmptyPickInstructions() {
            // Arrange
            List<PickInstruction> instructions = new ArrayList<>();

            // Act
            List<PickInstruction> optimizedRoute = optimizer.optimizeRoute(instructions);

            // Assert
            assertThat(optimizedRoute).isEmpty();
        }

        @Test
        @DisplayName("Should handle null pick instructions")
        void shouldHandleNullPickInstructions() {
            // Act & Assert
            assertThatThrownBy(() -> optimizer.optimizeRoute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pick instructions cannot be null");
        }
    }

    @Nested
    @DisplayName("Quantity Handling")
    class QuantityHandling {
        @Test
        @DisplayName("Should preserve total quantity across optimized route")
        void shouldPreserveTotalQuantity() {
            // Arrange
            List<PickInstruction> instructions = new ArrayList<>();
            instructions.add(createPickInstruction("SKU-001", "A01", 2));
            instructions.add(createPickInstruction("SKU-002", "B02", 3));
            instructions.add(createPickInstruction("SKU-003", "A01", 1));

            // Act
            List<PickInstruction> optimizedRoute = optimizer.optimizeRoute(instructions);

            // Assert
            int originalTotalQuantity = instructions.stream()
                .mapToInt(PickInstruction::getQuantity)
                .sum();
            int optimizedTotalQuantity = optimizedRoute.stream()
                .mapToInt(PickInstruction::getQuantity)
                .sum();

            assertThat(optimizedTotalQuantity).isEqualTo(originalTotalQuantity);
        }
    }

    // Helper method to create pick instructions
    private PickInstruction createPickInstruction(String skuCode, String binLocation, int quantity) {
        return new PickInstruction(
            SkuCode.of(skuCode), 
            new BinLocation(binLocation), 
            quantity
        );
    }
}