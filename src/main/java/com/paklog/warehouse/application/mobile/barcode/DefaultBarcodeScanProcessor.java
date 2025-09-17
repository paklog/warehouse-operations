package com.paklog.warehouse.application.mobile.barcode;

import com.paklog.warehouse.application.mobile.dto.MobileScanRequest;
import com.paklog.warehouse.application.mobile.dto.MobileScanResultDto;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DefaultBarcodeScanProcessor implements BarcodeScanProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DefaultBarcodeScanProcessor.class);
    
    // Regex patterns for different barcode types
    private static final Pattern ITEM_PATTERN = Pattern.compile("^[A-Z0-9]{3,20}$");
    private static final Pattern LOCATION_PATTERN = Pattern.compile("^[A-Z]\\d{2}-\\d{1,2}$");
    private static final Pattern LICENSE_PLATE_PATTERN = Pattern.compile("^LP[A-Z0-9]{8,12}$");
    
    private final ItemMasterService itemMasterService;
    private final LocationMasterService locationMasterService;

    public DefaultBarcodeScanProcessor(ItemMasterService itemMasterService, 
                                     LocationMasterService locationMasterService) {
        this.itemMasterService = itemMasterService;
        this.locationMasterService = locationMasterService;
    }

    @Override
    public MobileScanResultDto processScan(MobileScanRequest request) {
        logger.debug("Processing scan: {} for expected type: {}", 
                    request.getScannedCode(), request.getExpectedType());
        
        if (request.getScannedCode() == null) {
            return MobileScanResultDto.invalid("", "Invalid format for " + request.getExpectedType(), null);
        }
        
        String scannedCode = request.getScannedCode().trim().toUpperCase();
        String expectedType = request.getExpectedType();
        
        if (!isValidFormat(scannedCode, expectedType)) {
            return MobileScanResultDto.invalid(request.getScannedCode(), 
                "Invalid format for " + expectedType, 
                getSuggestions(scannedCode, expectedType));
        }
        
        String resolvedValue = resolveCode(scannedCode, expectedType);
        
        if (resolvedValue == null) {
            return MobileScanResultDto.invalid(request.getScannedCode(), 
                expectedType + " not found in system", 
                getSuggestions(scannedCode, expectedType));
        }
        
        // Additional validation based on work context
        ValidationResult contextValidation = validateInContext(request, resolvedValue);
        if (!contextValidation.isValid()) {
            MobileScanResultDto result = MobileScanResultDto.invalid(request.getScannedCode(), 
                contextValidation.getMessage(), contextValidation.getSuggestions());
            result.setRequiresConfirmation(contextValidation.isAllowOverride());
            result.setConfirmationMessage(contextValidation.getConfirmationMessage());
            return result;
        }
        
        return MobileScanResultDto.valid(request.getScannedCode(), resolvedValue, expectedType, 
            "Valid " + expectedType + " scan");
    }

    @Override
    public boolean isValidFormat(String scannedCode, String expectedType) {
        if (scannedCode == null || scannedCode.trim().isEmpty()) {
            return false;
        }
        
        return switch (expectedType) {
            case "ITEM" -> ITEM_PATTERN.matcher(scannedCode).matches();
            case "LOCATION" -> LOCATION_PATTERN.matcher(scannedCode).matches();
            case "LICENSE_PLATE" -> LICENSE_PLATE_PATTERN.matcher(scannedCode).matches();
            default -> false;
        };
    }

    @Override
    public String resolveCode(String scannedCode, String expectedType) {
        return switch (expectedType) {
            case "ITEM" -> resolveItemCode(scannedCode);
            case "LOCATION" -> resolveLocationCode(scannedCode);
            case "LICENSE_PLATE" -> resolveLicensePlateCode(scannedCode);
            default -> null;
        };
    }

    @Override
    public List<String> getSuggestions(String scannedCode, String expectedType) {
        return switch (expectedType) {
            case "ITEM" -> getItemSuggestions(scannedCode);
            case "LOCATION" -> getLocationSuggestions(scannedCode);
            case "LICENSE_PLATE" -> getLicensePlateSuggestions(scannedCode);
            default -> List.of("Check barcode and try again");
        };
    }

    private String resolveItemCode(String scannedCode) {
        try {
            // Check if it's a valid SKU code
            if (itemMasterService.isValidSku(scannedCode)) {
                return scannedCode;
            }
            
            // Try to find by alternative identifiers (UPC, EAN, etc.)
            String resolvedSku = itemMasterService.findSkuByAlternateCode(scannedCode);
            return resolvedSku;
            
        } catch (Exception e) {
            logger.warn("Error resolving item code {}: {}", scannedCode, e.getMessage());
            return null;
        }
    }

    private String resolveLocationCode(String scannedCode) {
        try {
            // Validate location exists and is active
            if (locationMasterService.isValidLocation(scannedCode)) {
                return scannedCode;
            }
            
            // Try alternative location formats
            String resolvedLocation = locationMasterService.findLocationByAlternateCode(scannedCode);
            return resolvedLocation;
            
        } catch (Exception e) {
            logger.warn("Error resolving location code {}: {}", scannedCode, e.getMessage());
            return null;
        }
    }

    private String resolveLicensePlateCode(String scannedCode) {
        try {
            // For now, assume any valid format LP is acceptable
            // In real implementation, would check against active license plates
            return scannedCode;
            
        } catch (Exception e) {
            logger.warn("Error resolving license plate code {}: {}", scannedCode, e.getMessage());
            return null;
        }
    }

    private ValidationResult validateInContext(MobileScanRequest request, String resolvedValue) {
        // Additional context-based validation would go here
        // For example, checking if the scanned location matches expected work location
        // or if the scanned item matches the work item
        
        if (request.getWorkId() != null && request.getExpectedType().equals("ITEM")) {
            // Could validate against work's expected item
            // For now, assume valid
        }
        
        if (request.getWorkId() != null && request.getExpectedType().equals("LOCATION")) {
            // Could validate against work's expected location
            // For now, assume valid
        }
        
        return ValidationResult.valid();
    }

    private List<String> getItemSuggestions(String scannedCode) {
        if (scannedCode.length() < 3) {
            return List.of("Item codes must be at least 3 characters", 
                          "Use alphanumeric characters only");
        }
        
        // Could query for similar items
        return List.of("Check if barcode is clear and readable", 
                      "Try scanning from different angle");
    }

    private List<String> getLocationSuggestions(String scannedCode) {
        if (!scannedCode.matches("^[A-Z].*")) {
            return List.of("Location should start with an aisle letter (A-Z)", 
                          "Format: A01-1, B02-3, etc.");
        }
        
        return List.of("Check location label is not damaged", 
                      "Format should be: Aisle-Bay-Level (e.g., A01-1)");
    }

    private List<String> getLicensePlateSuggestions(String scannedCode) {
        if (!scannedCode.startsWith("LP")) {
            return List.of("License plate should start with 'LP'", 
                          "Format: LP followed by 8-12 characters");
        }
        
        return List.of("Check license plate barcode is not damaged", 
                      "Try scanning the backup barcode if available");
    }

    // Helper class for validation results
    private static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final List<String> suggestions;
        private final boolean allowOverride;
        private final String confirmationMessage;

        private ValidationResult(boolean valid, String message, List<String> suggestions, 
                               boolean allowOverride, String confirmationMessage) {
            this.valid = valid;
            this.message = message;
            this.suggestions = suggestions;
            this.allowOverride = allowOverride;
            this.confirmationMessage = confirmationMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null, null, false, null);
        }

        public static ValidationResult invalid(String message, List<String> suggestions) {
            return new ValidationResult(false, message, suggestions, false, null);
        }

        public static ValidationResult invalidWithOverride(String message, String confirmationMessage) {
            return new ValidationResult(false, message, null, true, confirmationMessage);
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public List<String> getSuggestions() { return suggestions; }
        public boolean isAllowOverride() { return allowOverride; }
        public String getConfirmationMessage() { return confirmationMessage; }
    }

    // Mock service interfaces - would be implemented with actual data access
    public interface ItemMasterService {
        boolean isValidSku(String sku);
        String findSkuByAlternateCode(String alternateCode);
    }

    public interface LocationMasterService {
        boolean isValidLocation(String location);
        String findLocationByAlternateCode(String alternateCode);
    }
}