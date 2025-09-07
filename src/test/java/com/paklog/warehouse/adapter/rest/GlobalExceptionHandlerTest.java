package com.paklog.warehouse.adapter.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("Illegal Argument Exception Handling")
    class IllegalArgumentExceptionHandling {

        @Test
        @DisplayName("Should handle IllegalArgumentException correctly")
        void shouldHandleIllegalArgumentExceptionCorrectly() {
            // Arrange
            String errorMessage = "Invalid argument provided";
            IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

            // Act
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleIllegalArgumentException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(400);
            assertThat(response.getBody().getError()).isEqualTo("Invalid input");
            assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
            assertThat(response.getBody().getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Illegal State Exception Handling")
    class IllegalStateExceptionHandling {

        @Test
        @DisplayName("Should handle IllegalStateException correctly")
        void shouldHandleIllegalStateExceptionCorrectly() {
            // Arrange
            String errorMessage = "Invalid state";
            IllegalStateException exception = new IllegalStateException(errorMessage);

            // Act
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleIllegalStateException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(409);
            assertThat(response.getBody().getError()).isEqualTo("Invalid operation");
            assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        }
    }

    @Nested
    @DisplayName("Method Argument Not Valid Exception Handling")
    class MethodArgumentNotValidExceptionHandling {

        @Test
        @DisplayName("Should handle validation errors correctly")
        void shouldHandleValidationErrorsCorrectly() {
            // Arrange
            FieldError fieldError1 = new FieldError("object", "field1", "Field1 is required");
            FieldError fieldError2 = new FieldError("object", "field2", "Field2 is invalid");

            when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

            // Act
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleValidationException(methodArgumentNotValidException);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(400);
            assertThat(response.getBody().getError()).isEqualTo("Validation failed");
            assertThat(response.getBody().getMessage()).contains("field1");
            assertThat(response.getBody().getMessage()).contains("field2");
        }
    }

    @Nested
    @DisplayName("Method Argument Type Mismatch Exception Handling")
    class MethodArgumentTypeMismatchExceptionHandling {

        @Test
        @DisplayName("Should handle type mismatch errors correctly")
        void shouldHandleTypeMismatchErrorsCorrectly() {
            // Arrange
            MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
            when(exception.getValue()).thenReturn("invalid-value");
            when(exception.getName()).thenReturn("parameterName");

            // Act
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleTypeMismatchException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(400);
            assertThat(response.getBody().getError()).isEqualTo("Invalid parameter type");
            assertThat(response.getBody().getMessage()).contains("invalid-value");
            assertThat(response.getBody().getMessage()).contains("parameterName");
        }
    }

    @Nested
    @DisplayName("Entity Not Found Exception Handling")
    class EntityNotFoundExceptionHandling {

        @Test
        @DisplayName("Should handle EntityNotFoundException correctly")
        void shouldHandleEntityNotFoundExceptionCorrectly() {
            // Arrange
            String errorMessage = "Entity not found";
            EntityNotFoundException exception = new EntityNotFoundException(errorMessage);

            // Act
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleEntityNotFoundException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(404);
            assertThat(response.getBody().getError()).isEqualTo("Resource not found");
            assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        }
    }

    @Nested
    @DisplayName("General Exception Handling")
    class GeneralExceptionHandling {

        @Test
        @DisplayName("Should handle general exceptions correctly")
        void shouldHandleGeneralExceptionsCorrectly() {
            // Arrange
            Exception exception = new Exception("Unexpected error");

            // Act
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleGeneralException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(500);
            assertThat(response.getBody().getError()).isEqualTo("Internal server error");
            assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
        }
    }

    @Nested
    @DisplayName("Error Response")
    class ErrorResponse {

        @Test
        @DisplayName("Should create error response with all fields")
        void shouldCreateErrorResponseWithAllFields() {
            // Arrange
            int status = 400;
            String error = "Bad Request";
            String message = "Test message";

            // Act
            GlobalExceptionHandler.ErrorResponse errorResponse = 
                new GlobalExceptionHandler.ErrorResponse(status, error, message, java.time.LocalDateTime.now());

            // Assert
            assertThat(errorResponse.getStatus()).isEqualTo(status);
            assertThat(errorResponse.getError()).isEqualTo(error);
            assertThat(errorResponse.getMessage()).isEqualTo(message);
            assertThat(errorResponse.getTimestamp()).isNotNull();
        }
    }
}