package com.paklog.warehouse.domain.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SKU Code Domain Tests")
class SkuCodeTest {

    @Nested
    @DisplayName("SKU Code Creation")
    class SkuCodeCreation {
        @Test
        @DisplayName("Should create a valid SKU code")
        void shouldCreateValidSkuCode() {
            // Arrange
            String skuValue = "PROD-123-ABC";

            // Act
            SkuCode skuCode = new SkuCode(skuValue);

            // Assert
            assertThat(skuCode).isNotNull();
            assertThat(skuCode.getValue()).isEqualTo(skuValue);
        }

        @Test
        @DisplayName("Should throw exception for null SKU code")
        void shouldThrowExceptionForNullSkuCode() {
            // Act & Assert
            assertThatThrownBy(() -> new SkuCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU code cannot be null");
        }
    }

    @Nested
    @DisplayName("SKU Code Equality")
    class SkuCodeEquality {
        @Test
        @DisplayName("Should consider equal SKU codes with same value")
        void shouldConsiderEqualSkuCodesWithSameValue() {
            // Arrange
            SkuCode skuCode1 = new SkuCode("PROD-123");
            SkuCode skuCode2 = new SkuCode("PROD-123");

            // Assert
            assertThat(skuCode1).isEqualTo(skuCode2);
            assertThat(skuCode1.hashCode()).isEqualTo(skuCode2.hashCode());
        }

        @Test
        @DisplayName("Should consider different SKU codes with different values")
        void shouldConsiderDifferentSkuCodesWithDifferentValues() {
            // Arrange
            SkuCode skuCode1 = new SkuCode("PROD-123");
            SkuCode skuCode2 = new SkuCode("PROD-456");

            // Assert
            assertThat(skuCode1).isNotEqualTo(skuCode2);
            assertThat(skuCode1.hashCode()).isNotEqualTo(skuCode2.hashCode());
        }
    }

    @Nested
    @DisplayName("SKU Code String Representation")
    class SkuCodeStringRepresentation {
        @Test
        @DisplayName("Should provide correct string representation")
        void shouldProvideCorrectStringRepresentation() {
            // Arrange
            String skuValue = "PROD-123-ABC";
            SkuCode skuCode = new SkuCode(skuValue);

            // Act & Assert
            assertThat(skuCode.toString()).isEqualTo(skuValue);
        }
    }
}