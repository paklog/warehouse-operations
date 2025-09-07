package com.paklog.warehouse.adapter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paklog.warehouse.application.service.PickListQueryService;
import com.paklog.warehouse.domain.picklist.ConfirmItemPick;
import com.paklog.warehouse.domain.picklist.ConfirmItemPickHandler;
import com.paklog.warehouse.domain.shared.PickList;
import com.paklog.warehouse.domain.shared.PickListId;
import com.paklog.warehouse.domain.shared.PickListStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PickListController.class)
@DisplayName("PickListController Tests")
class PickListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PickListQueryService pickListQueryService;

    @MockBean
    private ConfirmItemPickHandler confirmItemPickHandler;

    @Nested
    @DisplayName("Get Pick List by ID")
    class GetPickListById {

        @Test
        @DisplayName("Should return pick list when found")
        void shouldReturnPickListWhenFound() throws Exception {
            // Arrange
            String pickListId = "test-pick-list-id";
            PickList pickList = mock(PickList.class);
            
            when(pickListQueryService.findById(any(PickListId.class))).thenReturn(pickList);

            // Act & Assert
            mockMvc.perform(get("/api/picklists/{pickListId}", pickListId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(pickListQueryService).findById(any(PickListId.class));
        }

        @Test
        @DisplayName("Should return 404 when pick list not found")
        void shouldReturn404WhenPickListNotFound() throws Exception {
            // Arrange
            String pickListId = "non-existent-id";
            
            when(pickListQueryService.findById(any(PickListId.class))).thenReturn(null);

            // Act & Assert
            mockMvc.perform(get("/api/picklists/{pickListId}", pickListId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 for blank pick list ID")
        void shouldReturn400ForBlankPickListId() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/picklists/{pickListId}", ""))
                    .andExpected(status().isNotFound()); // Path variable results in 404
        }
    }

    @Nested
    @DisplayName("Get Pick Lists by Picker")
    class GetPickListsByPicker {

        @Test
        @DisplayName("Should return pick lists for valid picker ID")
        void shouldReturnPickListsForValidPickerId() throws Exception {
            // Arrange
            String pickerId = "picker-001";
            PickList pickList1 = mock(PickList.class);
            PickList pickList2 = mock(PickList.class);
            
            when(pickListQueryService.findByPickerId(pickerId)).thenReturn(Arrays.asList(pickList1, pickList2));

            // Act & Assert
            mockMvc.perform(get("/api/picklists/picker/{pickerId}", pickerId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2));

            verify(pickListQueryService).findByPickerId(pickerId);
        }

        @Test
        @DisplayName("Should handle validation error for blank picker ID")
        void shouldHandleValidationErrorForBlankPickerId() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/picklists/picker/{pickerId}", ""))
                    .andExpect(status().isNotFound()); // Path variable results in 404
        }
    }

    @Nested
    @DisplayName("Get Pick Lists by Status")
    class GetPickListsByStatus {

        @Test
        @DisplayName("Should return pick lists for valid status")
        void shouldReturnPickListsForValidStatus() throws Exception {
            // Arrange
            PickListStatus status = PickListStatus.PENDING;
            PickList pickList = mock(PickList.class);
            
            when(pickListQueryService.findByStatus(status)).thenReturn(Arrays.asList(pickList));

            // Act & Assert
            mockMvc.perform(get("/api/picklists/status/{status}", status.name()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(1));

            verify(pickListQueryService).findByStatus(status);
        }

        @Test
        @DisplayName("Should handle invalid status gracefully")
        void shouldHandleInvalidStatusGracefully() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/picklists/status/{status}", "INVALID_STATUS"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Confirm Item Pick")
    class ConfirmItemPick {

        @Test
        @DisplayName("Should confirm item pick successfully")
        void shouldConfirmItemPickSuccessfully() throws Exception {
            // Arrange
            String pickListId = "test-pick-list-id";
            PickListController.ConfirmItemPickRequest request = new PickListController.ConfirmItemPickRequest();
            request.setSkuCode("SKU-001");
            request.setQuantity(2);
            request.setBinLocation("A1-B2");

            // Act & Assert
            mockMvc.perform(post("/api/picklists/{pickListId}/confirm-pick", pickListId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(confirmItemPickHandler).handle(any(ConfirmItemPick.class));
        }

        @Test
        @DisplayName("Should validate required fields")
        void shouldValidateRequiredFields() throws Exception {
            // Arrange
            String pickListId = "test-pick-list-id";
            PickListController.ConfirmItemPickRequest invalidRequest = new PickListController.ConfirmItemPickRequest();
            // Leaving fields null/invalid

            // Act & Assert
            mockMvc.perform(post("/api/picklists/{pickListId}/confirm-pick", pickListId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(confirmItemPickHandler, never()).handle(any());
        }

        @Test
        @DisplayName("Should validate positive quantity")
        void shouldValidatePositiveQuantity() throws Exception {
            // Arrange
            String pickListId = "test-pick-list-id";
            PickListController.ConfirmItemPickRequest request = new PickListController.ConfirmItemPickRequest();
            request.setSkuCode("SKU-001");
            request.setQuantity(-1); // Invalid negative quantity
            request.setBinLocation("A1-B2");

            // Act & Assert
            mockMvc.perform(post("/api/picklists/{pickListId}/confirm-pick", pickListId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(confirmItemPickHandler, never()).handle(any());
        }

        @Test
        @DisplayName("Should handle handler exceptions gracefully")
        void shouldHandleHandlerExceptionsGracefully() throws Exception {
            // Arrange
            String pickListId = "test-pick-list-id";
            PickListController.ConfirmItemPickRequest request = new PickListController.ConfirmItemPickRequest();
            request.setSkuCode("SKU-001");
            request.setQuantity(2);
            request.setBinLocation("A1-B2");

            doThrow(new IllegalArgumentException("Invalid pick"))
                    .when(confirmItemPickHandler).handle(any(ConfirmItemPick.class));

            // Act & Assert
            mockMvc.perform(post("/api/picklists/{pickListId}/confirm-pick", pickListId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Get Next Pick List for Picker")
    class GetNextPickListForPicker {

        @Test
        @DisplayName("Should return next pick list when available")
        void shouldReturnNextPickListWhenAvailable() throws Exception {
            // Arrange
            String pickerId = "picker-001";
            PickList pickList = mock(PickList.class);
            
            when(pickListQueryService.findNextPickListForPicker(pickerId))
                    .thenReturn(Optional.of(pickList));

            // Act & Assert
            mockMvc.perform(get("/api/picklists/picker/{pickerId}/next", pickerId))
                    .andExpect(status().isOk())
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON));

            verify(pickListQueryService).findNextPickListForPicker(pickerId);
        }

        @Test
        @DisplayName("Should return 204 when no pick list available")
        void shouldReturn204WhenNoPickListAvailable() throws Exception {
            // Arrange
            String pickerId = "picker-001";
            
            when(pickListQueryService.findNextPickListForPicker(pickerId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            mockMvc.perform(get("/api/picklists/picker/{pickerId}/next", pickerId))
                    .andExpect(status().isNoContent());
        }
    }
}