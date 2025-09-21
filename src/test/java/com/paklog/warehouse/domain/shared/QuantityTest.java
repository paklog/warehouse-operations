package com.paklog.warehouse.domain.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Quantity Domain Tests")
class QuantityTest {

    @Nested
    @DisplayName("Quantity Creation")
    class QuantityCreation {

        @Test
        @DisplayName("Should create valid positive quantity")
        void shouldCreateValidPositiveQuantity() {
            // Act
            Quantity quantity = Quantity.of(5);

            // Assert
            assertThat(quantity).isNotNull();
            assertThat(quantity.getValue()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should throw exception for negative quantity")
        void shouldThrowExceptionForNegativeQuantity() {
            // Act & Assert
            assertThatThrownBy(() -> Quantity.of(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity cannot be negative");
        }

        @Test
        @DisplayName("Should allow zero quantity")
        void shouldAllowZeroQuantity() {
            // Act
            Quantity quantity = Quantity.of(0);

            // Assert
            assertThat(quantity.getValue()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Quantity Operations")
    class QuantityOperations {

        @Test
        @DisplayName("Should add quantities correctly")
        void shouldAddQuantitiesCorrectly() {
            // Arrange
            Quantity quantity1 = Quantity.of(3);
            Quantity quantity2 = Quantity.of(7);

            // Act
            Quantity result = quantity1.add(quantity2);

            // Assert
            assertThat(result.getValue()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should subtract quantities correctly")
        void shouldSubtractQuantitiesCorrectly() {
            // Arrange
            Quantity quantity1 = Quantity.of(10);
            Quantity quantity2 = Quantity.of(3);

            // Act
            Quantity result = quantity1.subtract(quantity2);

            // Assert
            assertThat(result.getValue()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should throw exception when subtraction results in negative")
        void shouldThrowExceptionWhenSubtractionResultsInNegative() {
            // Arrange
            Quantity quantity1 = Quantity.of(3);
            Quantity quantity2 = Quantity.of(5);

            // Act & Assert
            assertThatThrownBy(() -> quantity1.subtract(quantity2))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should multiply quantity correctly")
        void shouldMultiplyQuantityCorrectly() {
            // Arrange
            Quantity quantity = Quantity.of(4);

            // Act
            Quantity result = quantity.multiply(3);

            // Assert
            assertThat(result.getValue()).isEqualTo(12);
        }

        @Test
        @DisplayName("Should throw exception when multiplying by zero or negative")
        void shouldThrowExceptionWhenMultiplyingByZeroOrNegative() {
            // Arrange
            Quantity quantity = Quantity.of(4);

            // Act & Assert
            assertThatThrownBy(() -> quantity.multiply(0))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> quantity.multiply(-1))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Quantity Comparison")
    class QuantityComparison {

        @Test
        @DisplayName("Should compare quantities correctly")
        void shouldCompareQuantitiesCorrectly() {
            // Arrange
            Quantity smaller = Quantity.of(3);
            Quantity larger = Quantity.of(7);
            Quantity equal = Quantity.of(3);

            // Assert
            assertThat(smaller.isLessThan(larger)).isTrue();
            assertThat(larger.isGreaterThan(smaller)).isTrue();
            assertThat(smaller.equals(equal)).isTrue();
            assertThat(smaller.isLessThan(equal)).isFalse();
        }
    }

    @Nested
    @DisplayName("Quantity Equality")
    class QuantityEquality {

        @Test
        @DisplayName("Should consider equal quantities with same value")
        void shouldConsiderEqualQuantitiesWithSameValue() {
            // Arrange
            Quantity quantity1 = Quantity.of(5);
            Quantity quantity2 = Quantity.of(5);

            // Assert
            assertThat(quantity1).isEqualTo(quantity2);
            assertThat(quantity1.hashCode()).isEqualTo(quantity2.hashCode());
        }

        @Test
        @DisplayName("Should consider different quantities with different values")
        void shouldConsiderDifferentQuantitiesWithDifferentValues() {
            // Arrange
            Quantity quantity1 = Quantity.of(3);
            Quantity quantity2 = Quantity.of(7);

            // Assert
            assertThat(quantity1).isNotEqualTo(quantity2);
        }
    }

    @Nested
    @DisplayName("Quantity String Representation")
    class QuantityStringRepresentation {

        @Test
        @DisplayName("Should provide correct string representation")
        void shouldProvideCorrectStringRepresentation() {
            // Arrange
            Quantity quantity = Quantity.of(42);

            // Act & Assert
            assertThat(quantity.toString()).contains("42");
        }
    }
}