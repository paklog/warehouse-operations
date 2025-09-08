package com.paklog.warehouse.adapter.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EntityNotFoundException Tests")
class EntityNotFoundExceptionTest {

    @Nested
    @DisplayName("Exception Creation")
    class ExceptionCreation {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateExceptionWithMessage() {
            // Arrange
            String message = "Entity not found";

            // Act
            EntityNotFoundException exception = new EntityNotFoundException(message);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            // Arrange
            String message = "Entity not found";
            Throwable cause = new RuntimeException("Database connection failed");

            // Act
            EntityNotFoundException exception = new EntityNotFoundException(message, cause);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("Should create pick list not found exception")
        void shouldCreatePickListNotFoundException() {
            // Arrange
            String pickListId = "pick-list-123";

            // Act
            EntityNotFoundException exception = EntityNotFoundException.pickListNotFound(pickListId);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo("PickList not found with ID: " + pickListId);
        }

        @Test
        @DisplayName("Should create package not found exception")
        void shouldCreatePackageNotFoundException() {
            // Arrange
            String packageId = "package-456";

            // Act
            EntityNotFoundException exception = EntityNotFoundException.packageNotFound(packageId);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo("Package not found with ID: " + packageId);
        }

    }

    @Nested
    @DisplayName("Exception Properties")
    class ExceptionProperties {

        @Test
        @DisplayName("Should be a RuntimeException")
        void shouldBeARuntimeException() {
            // Arrange & Act
            EntityNotFoundException exception = new EntityNotFoundException("test");

            // Assert
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should maintain inheritance chain")
        void shouldMaintainInheritanceChain() {
            // Arrange & Act
            EntityNotFoundException exception = new EntityNotFoundException("test");

            // Assert
            assertThat(exception).isInstanceOf(Exception.class);
            assertThat(exception).isInstanceOf(Throwable.class);
        }
    }
}