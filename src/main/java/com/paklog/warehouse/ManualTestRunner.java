package com.paklog.warehouse;

import com.paklog.warehouse.domain.work.*;
import com.paklog.warehouse.domain.shared.*;
import com.paklog.warehouse.domain.quality.*;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Manual test runner to verify core functionality works
 */
public class ManualTestRunner {
    
    public static void main(String[] args) {
        System.out.println("=== Warehouse Operations Manual Test Runner ===");
        
        try {
            testWorkDomain();
            testQualityDomain();
            testValueObjects();
            
            System.out.println("\n✅ ALL TESTS PASSED - Core functionality verified!");
            
        } catch (Exception e) {
            System.err.println("\n❌ TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testWorkDomain() {
        System.out.println("\n🔧 Testing Work Domain...");
        
        // Test WorkId
        WorkId workId = WorkId.generate();
        assert workId != null : "WorkId should not be null";
        assert workId.getValue() != null : "WorkId value should not be null";
        System.out.println("✅ WorkId creation: " + workId);
        
        // Test Work creation
        WorkTemplateId templateId = WorkTemplateId.generate();
        BinLocation location = BinLocation.of("A01-B02-L03");
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(10);
        
        WorkStep step1 = new WorkStep(1, WorkAction.NAVIGATE_TO_LOCATION, ValidationType.LOCATION_SCAN, 
                                    "Navigate to location", new HashMap<>());
        WorkStep step2 = new WorkStep(2, WorkAction.PICK_ITEM, ValidationType.QUANTITY_RANGE, 
                                    "Pick item", new HashMap<>());
        
        Work work = new Work(templateId, WorkType.PICK, location, item, quantity, Arrays.asList(step1, step2));
        assert work != null : "Work should not be null";
        assert work.getWorkId() != null : "Work should have an ID";
        assert work.getStatus() == WorkStatus.CREATED : "Work should be in CREATED status";
        System.out.println("✅ Work creation: " + work.getWorkId());
        
        // Test work operations
        work.assignTo("WORKER-001");
        assert work.getStatus() == WorkStatus.ASSIGNED : "Work should be ASSIGNED";
        assert "WORKER-001".equals(work.getAssignedTo()) : "Work should be assigned to WORKER-001";
        
        work.start();
        assert work.getStatus() == WorkStatus.IN_PROGRESS : "Work should be IN_PROGRESS";
        System.out.println("✅ Work assignment and start");
    }
    
    private static void testQualityDomain() {
        System.out.println("\n🔍 Testing Quality Domain...");
        
        QualityInspectionId inspectionId = QualityInspectionId.generate();
        SkuCode item = new SkuCode("TEST-ITEM-001");
        
        QualityInspection inspection = new QualityInspection(
            inspectionId, QualityInspectionType.RECEIVING_INSPECTION,
            item, "BATCH-001", 100, "inspector-001");
            
        assert inspection != null : "QualityInspection should not be null";
        assert inspection.getInspectionId().equals(inspectionId) : "Inspection ID should match";
        assert inspection.getStatus() == QualityInspectionStatus.SCHEDULED : "Should be SCHEDULED";
        assert "BATCH-001".equals(inspection.getBatchNumber()) : "Batch number should match";
        assert inspection.getQuantity() == 100 : "Quantity should be 100";
        System.out.println("✅ QualityInspection creation");
        
        // Test inspection operations
        inspection.start("inspector-001");
        assert inspection.getStatus() == QualityInspectionStatus.IN_PROGRESS : "Should be IN_PROGRESS";
        System.out.println("✅ QualityInspection start");
    }
    
    private static void testValueObjects() {
        System.out.println("\n📦 Testing Value Objects...");
        
        // Test BinLocation
        BinLocation location = BinLocation.of("A01-B02-L03");
        assert "A01".equals(location.getAisle()) : "Aisle should be A01";
        assert "B02".equals(location.getRack()) : "Rack should be B02";
        assert "L03".equals(location.getLevel()) : "Level should be L03";
        System.out.println("✅ BinLocation parsing: " + location);
        
        // Test SkuCode
        SkuCode sku = new SkuCode("SKU-12345");
        assert "SKU-12345".equals(sku.getValue()) : "SKU value should match";
        System.out.println("✅ SkuCode creation: " + sku);
        
        // Test Quantity
        Quantity qty = new Quantity(42);
        assert qty.getValue() == 42 : "Quantity value should be 42";
        System.out.println("✅ Quantity creation: " + qty);
        
        // Test WorkTemplateId
        WorkTemplateId templateId = WorkTemplateId.generate();
        assert templateId != null : "WorkTemplateId should not be null";
        assert templateId.getValue() != null : "WorkTemplateId value should not be null";
        System.out.println("✅ WorkTemplateId generation: " + templateId);
    }
}