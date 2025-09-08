package com.paklog.warehouse.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthCheckConfig Tests")
class HealthCheckConfigTest {

    @Test
    @DisplayName("Should create HealthCheckConfig successfully")
    void shouldCreateHealthCheckConfigSuccessfully() {
        // Act
        HealthCheckConfig config = new HealthCheckConfig();

        // Assert
        assertThat(config).isNotNull();
    }
    
    @Test  
    @DisplayName("HealthCheckConfig should be a valid Spring configuration")
    void shouldBeValidSpringConfiguration() {
        // Act & Assert - this test verifies that the class is properly annotated
        assertThat(HealthCheckConfig.class.isAnnotationPresent(org.springframework.context.annotation.Configuration.class))
            .isTrue();
    }
}