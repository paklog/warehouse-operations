package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class QualityQuarantineTest {

    private QualityQuarantine quarantine;
    private QualityQuarantineId quarantineId;
    private SkuCode item;

    @BeforeEach
    void setUp() {
        quarantineId = QualityQuarantineId.generate();
        item = new SkuCode("TEST-ITEM-001");
        
        quarantine = new QualityQuarantine(
            quarantineId,
            item,
            "BATCH-001",
            50,
            QualityQuarantineReason.FAILED_INSPECTION,
            "qc-officer-001"
        );
    }

    @Test
    void shouldCreateQuarantineWithCorrectInitialState() {
        assertEquals(quarantineId, quarantine.getQuarantineId());
        assertEquals(item, quarantine.getItem());
        assertEquals("BATCH-001", quarantine.getBatchNumber());
        assertEquals(50, quarantine.getQuantity());
        assertEquals(QualityQuarantineReason.FAILED_INSPECTION, quarantine.getReason());
        assertEquals("qc-officer-001", quarantine.getQuarantinedBy());
        assertEquals(QualityQuarantineStatus.ACTIVE, quarantine.getStatus());
        assertNotNull(quarantine.getQuarantinedAt());
        assertNotNull(quarantine.getExpiryDate());
        assertFalse(quarantine.isExpired());
        assertTrue(quarantine.isActive());
    }

    @Test
    void shouldMoveToQuarantineLocation() {
        Location quarantineLocation = new Location("QT", "001", "001");
        
        quarantine.moveToLocation(quarantineLocation);
        
        assertEquals(quarantineLocation, quarantine.getQuarantineLocation());
    }

    @Test
    void shouldNotMoveIfNotActive() {
        quarantine.release("qc-officer-001", "Issue resolved");
        Location quarantineLocation = new Location("QT", "001", "001");
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            quarantine.moveToLocation(quarantineLocation);
        });
        
        assertTrue(exception.getMessage().contains("Cannot move quarantined item"));
    }

    @Test
    void shouldReleaseQuarantine() {
        quarantine.release("qc-supervisor-001", "Root cause identified and resolved");
        
        assertEquals(QualityQuarantineStatus.RELEASED, quarantine.getStatus());
        assertEquals("qc-supervisor-001", quarantine.getReleasedBy());
        assertEquals("Root cause identified and resolved", quarantine.getReleaseNotes());
        assertNotNull(quarantine.getReleasedAt());
        assertFalse(quarantine.isActive());
    }

    @Test
    void shouldDisposeQuarantinedItem() {
        quarantine.dispose("qc-supervisor-001", "Item damaged beyond repair");
        
        assertEquals(QualityQuarantineStatus.DISPOSED, quarantine.getStatus());
        assertEquals("qc-supervisor-001", quarantine.getDisposedBy());
        assertEquals("Item damaged beyond repair", quarantine.getDisposeNotes());
        assertNotNull(quarantine.getDisposedAt());
        assertFalse(quarantine.isActive());
    }

    @Test
    void shouldExtendQuarantinePeriod() {
        Instant originalExpiry = quarantine.getExpiryDate();
        Instant newExpiry = originalExpiry.plusSeconds(7 * 24 * 3600); // +7 days
        
        quarantine.extendPeriod(newExpiry, "qc-supervisor-001", "Additional investigation required");
        
        assertEquals(newExpiry, quarantine.getExpiryDate());
        assertFalse(quarantine.isExpired());
    }

    @Test
    void shouldDetectExpiredQuarantine() {
        // Create a quarantine with past expiry date by extending to past
        Instant pastDate = Instant.now().minusSeconds(3600); // 1 hour ago
        quarantine.extendPeriod(pastDate, "test", "test");
        
        quarantine.checkExpiry();
        
        assertTrue(quarantine.isExpired());
    }

    @Test
    void shouldNotReleaseIfNotActive() {
        quarantine.dispose("qc-supervisor-001", "Disposed");
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            quarantine.release("qc-supervisor-001", "Trying to release");
        });
        
        assertTrue(exception.getMessage().contains("Cannot release quarantine"));
    }

    @Test
    void shouldNotDisposeIfNotActive() {
        quarantine.release("qc-supervisor-001", "Released");
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            quarantine.dispose("qc-supervisor-001", "Trying to dispose");
        });
        
        assertTrue(exception.getMessage().contains("Cannot dispose quarantine"));
    }

    @Test
    void shouldNotExtendIfNotActive() {
        quarantine.release("qc-supervisor-001", "Released");
        Instant newExpiry = Instant.now().plusSeconds(7 * 24 * 3600);
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            quarantine.extendPeriod(newExpiry, "qc-supervisor-001", "Trying to extend");
        });
        
        assertTrue(exception.getMessage().contains("Cannot extend quarantine"));
    }

    @Test
    void shouldCalculateCorrectExpiryForDifferentReasons() {
        // Test contamination suspected (14 days)
        QualityQuarantine contaminationQuarantine = new QualityQuarantine(
            QualityQuarantineId.generate(),
            item,
            "BATCH-002",
            25,
            QualityQuarantineReason.CONTAMINATION_SUSPECTED,
            "qc-officer-001"
        );
        
        assertTrue(contaminationQuarantine.getExpiryDate().isAfter(
            contaminationQuarantine.getQuarantinedAt().plusSeconds(13 * 24 * 3600))); // > 13 days
        
        // Test supplier recall (30 days)
        QualityQuarantine recallQuarantine = new QualityQuarantine(
            QualityQuarantineId.generate(),
            item,
            "BATCH-003",
            75,
            QualityQuarantineReason.SUPPLIER_RECALL,
            "qc-officer-001"
        );
        
        assertTrue(recallQuarantine.getExpiryDate().isAfter(
            recallQuarantine.getQuarantinedAt().plusSeconds(29 * 24 * 3600))); // > 29 days
    }
}