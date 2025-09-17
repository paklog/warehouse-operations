package com.paklog.warehouse.domain.location;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.work.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LocationDirectiveTest {

    private LocationDirective directive;
    private LocationConstraint zoneConstraint;
    private LocationConstraint capacityConstraint;

    @BeforeEach
    void setUp() {
        directive = new LocationDirective("Pick Directive", "Standard picking locations", 
                                        WorkType.PICK, LocationStrategy.NEAREST_EMPTY, 100);
        
        zoneConstraint = new LocationConstraint(LocationConstraintType.ZONE_RESTRICTION, 
                                              "equals", "PICK_ZONE");
        capacityConstraint = new LocationConstraint(LocationConstraintType.CAPACITY_REQUIREMENT, 
                                                  "gte", 10.0);
    }

    @Test
    void shouldCreateLocationDirectiveWithValidData() {
        assertNotNull(directive.getId());
        assertEquals("Pick Directive", directive.getName());
        assertEquals("Standard picking locations", directive.getDescription());
        assertEquals(WorkType.PICK, directive.getWorkType());
        assertEquals(LocationStrategy.NEAREST_EMPTY, directive.getStrategy());
        assertEquals(100, directive.getPriority());
        assertTrue(directive.isActive());
        assertNotNull(directive.getCreatedAt());
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThrows(NullPointerException.class, () -> 
            new LocationDirective(null, "Description", WorkType.PICK, LocationStrategy.FIXED, 100));
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> 
            new LocationDirective("", "Description", WorkType.PICK, LocationStrategy.FIXED, 100));
    }

    @Test
    void shouldThrowExceptionWhenWorkTypeIsNull() {
        assertThrows(NullPointerException.class, () -> 
            new LocationDirective("Name", "Description", null, LocationStrategy.FIXED, 100));
    }

    @Test
    void shouldThrowExceptionWhenStrategyIsNull() {
        assertThrows(NullPointerException.class, () -> 
            new LocationDirective("Name", "Description", WorkType.PICK, null, 100));
    }

    @Test
    void shouldThrowExceptionWhenPriorityIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> 
            new LocationDirective("Name", "Description", WorkType.PICK, LocationStrategy.FIXED, 0));
    }

    @Test
    void shouldAddConstraintSuccessfully() {
        directive.addConstraint(zoneConstraint);
        
        assertEquals(1, directive.getConstraintCount());
        assertEquals(zoneConstraint, directive.getConstraints().get(0));
    }

    @Test
    void shouldAddMultipleConstraints() {
        directive.addConstraint(zoneConstraint);
        directive.addConstraint(capacityConstraint);
        
        assertEquals(2, directive.getConstraintCount());
        assertTrue(directive.getConstraints().contains(zoneConstraint));
        assertTrue(directive.getConstraints().contains(capacityConstraint));
    }

    @Test
    void shouldRemoveConstraintSuccessfully() {
        directive.addConstraint(zoneConstraint);
        directive.addConstraint(capacityConstraint);
        
        directive.removeConstraint(zoneConstraint);
        
        assertEquals(1, directive.getConstraintCount());
        assertEquals(capacityConstraint, directive.getConstraints().get(0));
    }

    @Test
    void shouldUpdateStrategySuccessfully() {
        directive.updateStrategy(LocationStrategy.BULK_LOCATION);
        assertEquals(LocationStrategy.BULK_LOCATION, directive.getStrategy());
    }

    @Test
    void shouldUpdatePrioritySuccessfully() {
        directive.updatePriority(200);
        assertEquals(200, directive.getPriority());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingPriorityToInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> 
            directive.updatePriority(0));
    }

    @Test
    void shouldActivateAndDeactivateDirective() {
        assertTrue(directive.isActive());
        
        directive.deactivate();
        assertFalse(directive.isActive());
        
        directive.activate();
        assertTrue(directive.isActive());
    }

    @Test
    void shouldUpdateNameSuccessfully() {
        directive.updateName("New Directive Name");
        assertEquals("New Directive Name", directive.getName());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNameToNull() {
        assertThrows(NullPointerException.class, () -> 
            directive.updateName(null));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNameToEmpty() {
        assertThrows(IllegalArgumentException.class, () -> 
            directive.updateName(""));
    }

    @Test
    void shouldReturnTrueForApplicableWorkType() {
        assertTrue(directive.isApplicableFor(WorkType.PICK));
        assertFalse(directive.isApplicableFor(WorkType.PUT));
    }

    @Test
    void shouldReturnFalseForApplicableWorkTypeWhenInactive() {
        directive.deactivate();
        assertFalse(directive.isApplicableFor(WorkType.PICK));
    }

    @Test
    void shouldSatisfyConstraintsWhenNoConstraints() {
        BinLocation location = new BinLocation("A", "01", "1");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("zone", "PICK_ZONE");
        attributes.put("available_capacity", 15.0);
        
        LocationContext context = new LocationContext(location, new SkuCode("SKU001"), 
                                                    attributes, java.util.Set.of());
        
        assertTrue(directive.satisfiesConstraints(context));
    }

    @Test
    void shouldSatisfyConstraintsWhenConstraintsAreMet() {
        directive.addConstraint(zoneConstraint);
        directive.addConstraint(capacityConstraint);
        
        BinLocation location = new BinLocation("A", "01", "1");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("zone", "PICK_ZONE");
        attributes.put("available_capacity", 15.0);
        
        LocationContext context = new LocationContext(location, new SkuCode("SKU001"), 
                                                    attributes, java.util.Set.of());
        
        assertTrue(directive.satisfiesConstraints(context));
    }

    @Test
    void shouldNotSatisfyConstraintsWhenConstraintsAreNotMet() {
        directive.addConstraint(zoneConstraint);
        directive.addConstraint(capacityConstraint);
        
        BinLocation location = new BinLocation("A", "01", "1");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("zone", "WRONG_ZONE"); // Wrong zone
        attributes.put("available_capacity", 5.0); // Insufficient capacity
        
        LocationContext context = new LocationContext(location, new SkuCode("SKU001"), 
                                                    attributes, java.util.Set.of());
        
        assertFalse(directive.satisfiesConstraints(context));
    }

    @Test
    void shouldNotSatisfyConstraintsWhenInactive() {
        directive.deactivate();
        
        BinLocation location = new BinLocation("A", "01", "1");
        Map<String, Object> attributes = new HashMap<>();
        LocationContext context = new LocationContext(location, new SkuCode("SKU001"), 
                                                    attributes, java.util.Set.of());
        
        assertFalse(directive.satisfiesConstraints(context));
    }

    @Test
    void shouldSelectLocationSuccessfully() {
        BinLocation location = new BinLocation("A", "01", "1");
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        
        LocationQuery query = new LocationQuery(WorkType.PICK, item, quantity);
        
        BinLocation selectedLocation = directive.selectLocation(query);
        
        assertNotNull(selectedLocation);
    }

    @Test
    void shouldReturnNullLocationForNonApplicableWorkType() {
        BinLocation location = new BinLocation("A", "01", "1");
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        
        LocationQuery query = new LocationQuery(WorkType.PUT, item, quantity); // Wrong work type
        
        BinLocation selectedLocation = directive.selectLocation(query);
        
        assertNull(selectedLocation);
    }

    @Test
    void shouldEvaluateLocationForQuery() {
        BinLocation location = new BinLocation("A", "01", "1");
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        
        LocationQuery query = new LocationQuery(WorkType.PICK, item, quantity);
        
        LocationDirectiveResult result = directive.evaluateForLocation(query, location);
        
        assertNotNull(result);
        assertTrue(result.isSuitable());
        assertTrue(result.getScore() > 0);
    }

    @Test
    void shouldReturnNotApplicableForWrongWorkType() {
        BinLocation location = new BinLocation("A", "01", "1");
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        
        LocationQuery query = new LocationQuery(WorkType.PUT, item, quantity); // Wrong work type
        
        LocationDirectiveResult result = directive.evaluateForLocation(query, location);
        
        assertNotNull(result);
        assertTrue(result.isNotApplicable());
    }

    @Test
    void shouldReturnConstraintViolationWhenConstraintsNotMet() {
        directive.addConstraint(zoneConstraint);
        
        BinLocation location = new BinLocation("A", "01", "1");
        SkuCode item = new SkuCode("SKU001");
        Quantity quantity = new Quantity(5);
        
        LocationQuery query = new LocationQuery(WorkType.PICK, item, quantity);
        
        LocationDirectiveResult result = directive.evaluateForLocation(query, location);
        
        assertNotNull(result);
        assertTrue(result.hasConstraintViolations());
        assertFalse(result.getViolations().isEmpty());
    }

    @Test
    void shouldEqualDirectivesWithSameId() {
        LocationDirective directive1 = new LocationDirective("Directive 1", "Description", 
                                                           WorkType.PICK, LocationStrategy.FIXED, 100);
        LocationDirective directive2 = new LocationDirective(directive1.getId(), "Directive 2", "Different", 
                                                           WorkType.PUT, LocationStrategy.RANDOM, 
                                                           java.util.List.of(), 200, true, 
                                                           java.time.Instant.now(), java.time.Instant.now(), 
                                                           "user", 1);
        
        assertEquals(directive1, directive2);
        assertEquals(directive1.hashCode(), directive2.hashCode());
    }

    @Test
    void shouldNotEqualDirectivesWithDifferentIds() {
        LocationDirective directive1 = new LocationDirective("Directive 1", "Description", 
                                                           WorkType.PICK, LocationStrategy.FIXED, 100);
        LocationDirective directive2 = new LocationDirective("Directive 2", "Description", 
                                                           WorkType.PICK, LocationStrategy.FIXED, 100);
        
        assertNotEquals(directive1, directive2);
    }
}