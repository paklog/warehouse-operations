package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.work.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationDirectiveServiceTest {

    @Mock
    private LocationDirectiveRepository repository;

    private LocationDirectiveService service;
    private LocationDirective pickDirective;
    private LocationDirective putDirective;

    @BeforeEach
    void setUp() {
        service = new LocationDirectiveService(repository);
        
        pickDirective = new LocationDirective("Pick Directive", "Standard picking", 
                                            WorkType.PICK, LocationStrategy.NEAREST_EMPTY, 100);
        putDirective = new LocationDirective("Put Directive", "Standard put away", 
                                           WorkType.PUT, LocationStrategy.BULK_LOCATION, 200);
    }

    @Test
    void shouldSelectOptimalLocationSuccessfully() {
        // Arrange
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        LocationQuery query = new LocationQuery(WorkType.PICK, item, quantity);
        
        when(repository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList(pickDirective));

        // Act
        BinLocation selectedLocation = service.selectOptimalLocation(query);

        // Assert
        assertNotNull(selectedLocation);
        verify(repository).findByWorkTypeAndActive(WorkType.PICK, true);
    }

    @Test
    void shouldReturnNullWhenNoApplicableDirectives() {
        // Arrange
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        LocationQuery query = new LocationQuery(WorkType.PICK, item, quantity);
        
        when(repository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList());

        // Act
        BinLocation selectedLocation = service.selectOptimalLocation(query);

        // Assert
        assertNull(selectedLocation);
        verify(repository).findByWorkTypeAndActive(WorkType.PICK, true);
    }

    @Test
    void shouldEvaluateLocationSuccessfully() {
        // Arrange
        BinLocation location = new BinLocation("A", "01", "1");
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        LocationQuery query = new LocationQuery(WorkType.PICK, item, quantity);
        
        when(repository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList(pickDirective));

        // Act
        LocationDirectiveService.LocationEvaluationResult result = 
            service.evaluateLocation(query, location);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuitable());
        assertTrue(result.getScore() > 0);
        assertEquals(1, result.getApplicableDirectives());
        verify(repository).findByWorkTypeAndActive(WorkType.PICK, true);
    }

    @Test
    void shouldReturnNoDirectivesAvailableWhenNoneFound() {
        // Arrange
        BinLocation location = new BinLocation("A", "01", "1");
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        LocationQuery query = new LocationQuery(WorkType.PICK, item, quantity);
        
        when(repository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList());

        // Act
        LocationDirectiveService.LocationEvaluationResult result = 
            service.evaluateLocation(query, location);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuitable());
        assertEquals(0, result.getScore());
        assertEquals(1, result.getViolations().size());
        assertTrue(result.getViolations().get(0).contains("No applicable directives"));
    }

    @Test
    void shouldFindBestLocationsSuccessfully() {
        // Arrange
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        BinLocation candidate1 = new BinLocation("A", "01", "1");
        BinLocation candidate2 = new BinLocation("A", "02", "1");
        
        LocationQuery query = new LocationQuery(WorkType.PICK, item, quantity, null, 
                                               new java.util.HashMap<>(), 
                                               Arrays.asList(candidate1, candidate2));
        
        when(repository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList(pickDirective));

        // Act
        List<BinLocation> bestLocations = service.findBestLocations(query, 5);

        // Assert
        assertNotNull(bestLocations);
        assertFalse(bestLocations.isEmpty());
        assertTrue(bestLocations.size() <= 5);
        verify(repository, atLeastOnce()).findByWorkTypeAndActive(WorkType.PICK, true);
    }

    @Test
    void shouldReturnTrueWhenCanSatisfyQuery() {
        // Arrange
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        LocationQuery query = new LocationQuery(WorkType.PICK, item, quantity);
        
        when(repository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList(pickDirective));

        // Act
        boolean canSatisfy = service.canSatisfyQuery(query);

        // Assert
        assertTrue(canSatisfy);
        verify(repository).findByWorkTypeAndActive(WorkType.PICK, true);
    }

    @Test
    void shouldReturnFalseWhenCannotSatisfyQuery() {
        // Arrange
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        LocationQuery query = new LocationQuery(WorkType.PICK, item, quantity);
        
        when(repository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList());

        // Act
        boolean canSatisfy = service.canSatisfyQuery(query);

        // Assert
        assertFalse(canSatisfy);
        verify(repository).findByWorkTypeAndActive(WorkType.PICK, true);
    }

    @Test
    void shouldGetApplicableDirectivesSortedByPriority() {
        // Arrange
        LocationDirective lowPriorityDirective = new LocationDirective("Low Priority", "Description", 
                                                                      WorkType.PICK, LocationStrategy.RANDOM, 50);
        LocationDirective highPriorityDirective = new LocationDirective("High Priority", "Description", 
                                                                       WorkType.PICK, LocationStrategy.FIXED, 300);
        
        when(repository.findByWorkTypeAndActive(WorkType.PICK, true))
            .thenReturn(Arrays.asList(highPriorityDirective, pickDirective, lowPriorityDirective));

        // Act
        List<LocationDirective> applicableDirectives = service.getApplicableDirectives(WorkType.PICK);

        // Assert
        assertEquals(3, applicableDirectives.size());
        assertEquals(lowPriorityDirective, applicableDirectives.get(0)); // Priority 50
        assertEquals(pickDirective, applicableDirectives.get(1)); // Priority 100
        assertEquals(highPriorityDirective, applicableDirectives.get(2)); // Priority 300
        verify(repository).findByWorkTypeAndActive(WorkType.PICK, true);
    }

    @Test
    void shouldValidateDirectiveSuccessfully() {
        // Arrange
        pickDirective.addConstraint(new LocationConstraint(LocationConstraintType.INVENTORY_AVAILABLE, "gt", 0));

        // Act
        LocationDirectiveService.LocationDirectiveValidationResult result = 
            service.validateDirective(pickDirective);

        // Assert
        assertNotNull(result);
        assertTrue(result.isValid());
        assertTrue(result.getIssues().isEmpty());
    }

    @Test
    void shouldFailValidationForInactiveDirective() {
        // Arrange
        pickDirective.deactivate();

        // Act
        LocationDirectiveService.LocationDirectiveValidationResult result = 
            service.validateDirective(pickDirective);

        // Assert
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.getIssues().contains("Directive is inactive"));
    }

    @Test
    void shouldFailValidationForStrategyWithoutRequiredConstraints() {
        // Arrange
        LocationDirective fifoDirective = new LocationDirective("FIFO Directive", "FIFO strategy", 
                                                              WorkType.PICK, LocationStrategy.FIFO, 100);
        // FIFO strategy requires inventory data but no inventory constraint added

        // Act
        LocationDirectiveService.LocationDirectiveValidationResult result = 
            service.validateDirective(fifoDirective);

        // Assert
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.getIssues().stream()
                  .anyMatch(issue -> issue.contains("Strategy requires inventory data")));
    }

    @Test
    void shouldFailValidationForZoneStrategyWithoutZoneConstraints() {
        // Arrange
        LocationDirective zoneDirective = new LocationDirective("Zone Directive", "Zone-based strategy", 
                                                              WorkType.PICK, LocationStrategy.ZONE_BASED, 100);
        // Zone-based strategy requires zone configuration but no zone constraint added

        // Act
        LocationDirectiveService.LocationDirectiveValidationResult result = 
            service.validateDirective(zoneDirective);

        // Assert
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.getIssues().stream()
                  .anyMatch(issue -> issue.contains("Strategy requires zone configuration")));
    }

    @Test
    void shouldCreateDefaultDirectiveForPick() {
        // Act
        LocationDirective defaultDirective = service.createDefaultDirective(WorkType.PICK, LocationStrategy.NEAREST_EMPTY);

        // Assert
        assertNotNull(defaultDirective);
        assertEquals(WorkType.PICK, defaultDirective.getWorkType());
        assertEquals(LocationStrategy.NEAREST_EMPTY, defaultDirective.getStrategy());
        assertTrue(defaultDirective.isActive());
        assertEquals(100, defaultDirective.getPriority());
        assertFalse(defaultDirective.getConstraints().isEmpty());
        
        // Should have accessibility and inventory constraints for PICK
        assertTrue(defaultDirective.getConstraints().stream()
                  .anyMatch(c -> c.getType() == LocationConstraintType.ACCESSIBILITY));
        assertTrue(defaultDirective.getConstraints().stream()
                  .anyMatch(c -> c.getType() == LocationConstraintType.INVENTORY_AVAILABLE));
    }

    @Test
    void shouldCreateDefaultDirectiveForPut() {
        // Act
        LocationDirective defaultDirective = service.createDefaultDirective(WorkType.PUT, LocationStrategy.BULK_LOCATION);

        // Assert
        assertNotNull(defaultDirective);
        assertEquals(WorkType.PUT, defaultDirective.getWorkType());
        assertEquals(LocationStrategy.BULK_LOCATION, defaultDirective.getStrategy());
        assertTrue(defaultDirective.isActive());
        
        // Should have capacity and accessibility constraints for PUT
        assertTrue(defaultDirective.getConstraints().stream()
                  .anyMatch(c -> c.getType() == LocationConstraintType.CAPACITY_REQUIREMENT));
        assertTrue(defaultDirective.getConstraints().stream()
                  .anyMatch(c -> c.getType() == LocationConstraintType.ACCESSIBILITY));
    }

    @Test
    void shouldCreateDefaultDirectiveForCount() {
        // Act
        LocationDirective defaultDirective = service.createDefaultDirective(WorkType.COUNT, LocationStrategy.RANDOM);

        // Assert
        assertNotNull(defaultDirective);
        assertEquals(WorkType.COUNT, defaultDirective.getWorkType());
        assertEquals(LocationStrategy.RANDOM, defaultDirective.getStrategy());
        
        // Should have accessibility constraint for COUNT
        assertTrue(defaultDirective.getConstraints().stream()
                  .anyMatch(c -> c.getType() == LocationConstraintType.ACCESSIBILITY));
    }

    @Test
    void shouldThrowExceptionWhenRepositoryIsNull() {
        assertThrows(NullPointerException.class, () -> 
            new LocationDirectiveService(null));
    }
}