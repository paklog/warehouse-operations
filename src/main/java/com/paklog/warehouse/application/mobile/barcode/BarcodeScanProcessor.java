package com.paklog.warehouse.application.mobile.barcode;

import com.paklog.warehouse.application.mobile.dto.MobileScanRequest;
import com.paklog.warehouse.application.mobile.dto.MobileScanResultDto;

public interface BarcodeScanProcessor {
    
    /**
     * Processes a barcode scan and validates it against expected values
     */
    MobileScanResultDto processScan(MobileScanRequest request);
    
    /**
     * Validates if a scanned code matches expected pattern for given type
     */
    boolean isValidFormat(String scannedCode, String expectedType);
    
    /**
     * Resolves a scanned code to its canonical form (e.g., item code to full SKU)
     */
    String resolveCode(String scannedCode, String expectedType);
    
    /**
     * Gets suggestions for invalid scans
     */
    java.util.List<String> getSuggestions(String scannedCode, String expectedType);
}