package com.paklog.warehouse.application.mobile.barcode;

import com.paklog.warehouse.application.mobile.dto.MobileScanRequest;
import com.paklog.warehouse.application.mobile.dto.MobileScanResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultBarcodeScanProcessorTest {

    @Mock
    private DefaultBarcodeScanProcessor.ItemMasterService itemMasterService;
    
    @Mock
    private DefaultBarcodeScanProcessor.LocationMasterService locationMasterService;

    private DefaultBarcodeScanProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new DefaultBarcodeScanProcessor(itemMasterService, locationMasterService);
    }

    @Test
    void shouldValidateItemBarcodeFormat() {
        // Test valid item formats
        assertTrue(processor.isValidFormat("SKU001", "ITEM"));
        assertTrue(processor.isValidFormat("ABC123DEF", "ITEM"));
        assertTrue(processor.isValidFormat("123456789012345", "ITEM"));
        
        // Test invalid item formats
        assertFalse(processor.isValidFormat("AB", "ITEM")); // Too short
        assertFalse(processor.isValidFormat("SKU001-INVALID-CODE", "ITEM")); // Too long
        assertFalse(processor.isValidFormat("SKU-001", "ITEM")); // Contains hyphen
        assertFalse(processor.isValidFormat("", "ITEM")); // Empty
        assertFalse(processor.isValidFormat(null, "ITEM")); // Null
    }

    @Test
    void shouldValidateLocationBarcodeFormat() {
        // Test valid location formats
        assertTrue(processor.isValidFormat("A01-1", "LOCATION"));
        assertTrue(processor.isValidFormat("B12-5", "LOCATION"));
        assertTrue(processor.isValidFormat("Z99-10", "LOCATION"));
        
        // Test invalid location formats
        assertFalse(processor.isValidFormat("1A1-1", "LOCATION")); // Doesn't start with letter
        assertFalse(processor.isValidFormat("AA1-1", "LOCATION")); // Two letters
        assertFalse(processor.isValidFormat("A1-1", "LOCATION")); // Single digit aisle
        assertFalse(processor.isValidFormat("A01", "LOCATION")); // Missing level
        assertFalse(processor.isValidFormat("A01-", "LOCATION")); // Missing level number
    }

    @Test
    void shouldValidateLicensePlateBarcodeFormat() {
        // Test valid license plate formats
        assertTrue(processor.isValidFormat("LP12345678", "LICENSE_PLATE"));
        assertTrue(processor.isValidFormat("LPABC123DEF", "LICENSE_PLATE"));
        assertTrue(processor.isValidFormat("LP123456789012", "LICENSE_PLATE"));
        
        // Test invalid license plate formats
        assertFalse(processor.isValidFormat("12345678", "LICENSE_PLATE")); // Missing LP prefix
        assertFalse(processor.isValidFormat("LP123", "LICENSE_PLATE")); // Too short
        assertFalse(processor.isValidFormat("LP1234567890123", "LICENSE_PLATE")); // Too long
        assertFalse(processor.isValidFormat("LP", "LICENSE_PLATE")); // Only prefix
    }

    @Test
    void shouldProcessValidItemScanSuccessfully() {
        // Arrange
        String scannedCode = "SKU001";
        MobileScanRequest request = new MobileScanRequest(
            "WORKER-001", "WORK-001", 1, scannedCode, "BARCODE", "ITEM", null);
        
        when(itemMasterService.isValidSku(scannedCode)).thenReturn(true);

        // Act
        MobileScanResultDto result = processor.processScan(request);

        // Assert
        assertTrue(result.isValid());
        assertEquals(scannedCode, result.getScannedValue());
        assertEquals(scannedCode, result.getResolvedValue());
        assertEquals("ITEM", result.getType());
        verify(itemMasterService).isValidSku(scannedCode);
    }

    @Test
    void shouldProcessValidLocationScanSuccessfully() {
        // Arrange
        String scannedCode = "A01-1";
        MobileScanRequest request = new MobileScanRequest(
            "WORKER-001", "WORK-001", 1, scannedCode, "BARCODE", "LOCATION", null);
        
        when(locationMasterService.isValidLocation(scannedCode)).thenReturn(true);

        // Act
        MobileScanResultDto result = processor.processScan(request);

        // Assert
        assertTrue(result.isValid());
        assertEquals(scannedCode, result.getScannedValue());
        assertEquals(scannedCode, result.getResolvedValue());
        assertEquals("LOCATION", result.getType());
        verify(locationMasterService).isValidLocation(scannedCode);
    }

    @Test
    void shouldRejectInvalidFormatScan() {
        // Arrange
        String scannedCode = "INVALID-FORMAT";
        MobileScanRequest request = new MobileScanRequest(
            "WORKER-001", "WORK-001", 1, scannedCode, "BARCODE", "ITEM", null);

        // Act
        MobileScanResultDto result = processor.processScan(request);

        // Assert
        assertFalse(result.isValid());
        assertEquals(scannedCode, result.getScannedValue());
        assertNull(result.getResolvedValue());
        assertTrue(result.getMessage().contains("Invalid format"));
        assertNotNull(result.getSuggestions());
        assertFalse(result.getSuggestions().isEmpty());
    }

    @Test
    void shouldRejectNonExistentItemScan() {
        // Arrange
        String scannedCode = "SKU999";
        MobileScanRequest request = new MobileScanRequest(
            "WORKER-001", "WORK-001", 1, scannedCode, "BARCODE", "ITEM", null);
        
        when(itemMasterService.isValidSku(scannedCode)).thenReturn(false);
        when(itemMasterService.findSkuByAlternateCode(scannedCode)).thenReturn(null);

        // Act
        MobileScanResultDto result = processor.processScan(request);

        // Assert
        assertFalse(result.isValid());
        assertEquals(scannedCode, result.getScannedValue());
        assertNull(result.getResolvedValue());
        assertTrue(result.getMessage().contains("not found"));
        verify(itemMasterService).isValidSku(scannedCode);
        verify(itemMasterService).findSkuByAlternateCode(scannedCode);
    }

    @Test
    void shouldResolveItemByAlternateCode() {
        // Arrange
        String scannedCode = "UPC123456";
        String resolvedSku = "SKU001";
        MobileScanRequest request = new MobileScanRequest(
            "WORKER-001", "WORK-001", 1, scannedCode, "BARCODE", "ITEM", null);
        
        when(itemMasterService.isValidSku(scannedCode)).thenReturn(false);
        when(itemMasterService.findSkuByAlternateCode(scannedCode)).thenReturn(resolvedSku);

        // Act
        MobileScanResultDto result = processor.processScan(request);

        // Assert
        assertTrue(result.isValid());
        assertEquals(scannedCode, result.getScannedValue());
        assertEquals(resolvedSku, result.getResolvedValue());
        assertEquals("ITEM", result.getType());
        verify(itemMasterService).findSkuByAlternateCode(scannedCode);
    }

    @Test
    void shouldResolveLocationByAlternateCode() {
        // Arrange
        String scannedCode = "A01-1";
        String resolvedLocation = "A01-1";
        MobileScanRequest request = new MobileScanRequest(
            "WORKER-001", "WORK-001", 1, scannedCode, "BARCODE", "LOCATION", null);
        
        when(locationMasterService.isValidLocation(scannedCode)).thenReturn(false);
        when(locationMasterService.findLocationByAlternateCode(scannedCode)).thenReturn(resolvedLocation);

        // Act
        MobileScanResultDto result = processor.processScan(request);

        // Assert
        assertTrue(result.isValid());
        assertEquals(scannedCode, result.getScannedValue());
        assertEquals(resolvedLocation, result.getResolvedValue());
        assertEquals("LOCATION", result.getType());
        verify(locationMasterService).findLocationByAlternateCode(scannedCode);
    }

    @Test
    void shouldProvideItemSuggestionsForInvalidScan() {
        // Arrange
        String shortCode = "AB";

        // Act
        List<String> suggestions = processor.getSuggestions(shortCode, "ITEM");

        // Assert
        assertFalse(suggestions.isEmpty());
        assertTrue(suggestions.stream().anyMatch(s -> s.contains("at least 3 characters")));
    }

    @Test
    void shouldProvideLocationSuggestionsForInvalidScan() {
        // Arrange
        String invalidCode = "123-1";

        // Act
        List<String> suggestions = processor.getSuggestions(invalidCode, "LOCATION");

        // Assert
        assertFalse(suggestions.isEmpty());
        assertTrue(suggestions.stream().anyMatch(s -> s.contains("aisle letter")));
    }

    @Test
    void shouldProvideLicensePlateSuggestionsForInvalidScan() {
        // Arrange
        String invalidCode = "12345678";

        // Act
        List<String> suggestions = processor.getSuggestions(invalidCode, "LICENSE_PLATE");

        // Assert
        assertFalse(suggestions.isEmpty());
        assertTrue(suggestions.stream().anyMatch(s -> s.contains("LP")));
    }

    @Test
    void shouldHandleEmptyScannedCode() {
        // Arrange
        MobileScanRequest request = new MobileScanRequest(
            "WORKER-001", "WORK-001", 1, "", "BARCODE", "ITEM", null);

        // Act
        MobileScanResultDto result = processor.processScan(request);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Invalid format"));
    }

    @Test
    void shouldHandleNullScannedCode() {
        // Arrange
        MobileScanRequest request = new MobileScanRequest(
            "WORKER-001", "WORK-001", 1, null, "BARCODE", "ITEM", null);

        // Act
        MobileScanResultDto result = processor.processScan(request);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Invalid format"));
    }

    @Test
    void shouldHandleUnknownExpectedType() {
        // Arrange
        String scannedCode = "TEST123";
        MobileScanRequest request = new MobileScanRequest(
            "WORKER-001", "WORK-001", 1, scannedCode, "BARCODE", "UNKNOWN_TYPE", null);

        // Act
        MobileScanResultDto result = processor.processScan(request);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Invalid format"));
    }

    @Test
    void shouldTrimAndUpperCaseScannedCode() {
        // Arrange
        String scannedCode = "  sku001  ";
        MobileScanRequest request = new MobileScanRequest(
            "WORKER-001", "WORK-001", 1, scannedCode, "BARCODE", "ITEM", null);
        
        when(itemMasterService.isValidSku("SKU001")).thenReturn(true);

        // Act
        MobileScanResultDto result = processor.processScan(request);

        // Assert
        assertTrue(result.isValid());
        assertEquals("  sku001  ", result.getScannedValue()); // Original value preserved
        assertEquals("SKU001", result.getResolvedValue()); // Processed value (trimmed and uppercased)
        verify(itemMasterService).isValidSku("SKU001");
    }
}