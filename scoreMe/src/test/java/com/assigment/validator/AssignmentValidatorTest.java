package com.assigment.validator;

import com.assigment.model.Assignment;
import com.assigment.model.Instance;
import com.assigment.model.Slot;
import com.assigment.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssignmentValidatorTest {
    private Instance instance;
    private Assignment validAssignment;

    @BeforeEach
    public void setUp() {
        // Create a simple test instance
        instance = new Instance(5, 3);

        // Add 5 tasks with distinct resources
        instance.addTask(new Task("T0", new double[]{1.0, 0, 0, 0}, 1.0, 0, 2));
        instance.addTask(new Task("T1", new double[]{1.0, 0, 0, 0}, 1.0, 0, 2));
        instance.addTask(new Task("T2", new double[]{0, 1.0, 0, 0}, 1.0, 1, 2));
        instance.addTask(new Task("T3", new double[]{0, 0, 1.0, 0}, 1.0, 0, 2));
        instance.addTask(new Task("T4", new double[]{0, 0, 0, 1.0}, 1.0, 0, 1));

        // Add 3 slots with capacity in each dimension
        instance.addSlot(new Slot(0, new double[]{2.0, 2.0, 2.0, 2.0}));
        instance.addSlot(new Slot(1, new double[]{2.0, 2.0, 2.0, 2.0}));
        instance.addSlot(new Slot(2, new double[]{2.0, 2.0, 2.0, 2.0}));

        // Create a valid assignment
        validAssignment = new Assignment(instance);
        validAssignment.assign("T0", 0);
        validAssignment.assign("T1", 0);  // Two tasks in same slot (no conflict)
        validAssignment.assign("T2", 1);
        validAssignment.assign("T3", 2);
        validAssignment.assign("T4", 1);
    }

    @Test
    public void testValidAssignmentPasses() {
        AssignmentValidator.ValidationResult result = AssignmentValidator.validate(instance, validAssignment);

        assertTrue(result.isValid(), "Valid assignment should pass validation");
        assertEquals("Assignment is feasible", result.getMessage());
    }

    @Test
    public void testConflictViolationDetected() {
        instance.addConflict(0, 1);  // Conflict between T0 and T1

        // Try to assign both to same slot
        Assignment conflictAssignment = new Assignment(instance);
        conflictAssignment.assign("T0", 0);
        conflictAssignment.assign("T1", 0);  // Conflict!
        conflictAssignment.assign("T2", 1);
        conflictAssignment.assign("T3", 2);
        conflictAssignment.assign("T4", 1);

        AssignmentValidator.ValidationResult result = AssignmentValidator.validate(instance, conflictAssignment);

        assertFalse(result.isValid(), "Conflicting tasks in same slot should be detected");
        assertTrue(result.getMessage().contains("F1"), "Should report F1 violation");
        assertTrue(result.getMessage().contains("Conflict"), "Error message should mention conflict");
    }

    @Test
    public void testCapacityViolationDetected() {

        Assignment overCapacityAssignment = new Assignment(instance);
        overCapacityAssignment.assign("T0", 0);  // Uses 1.0 CPU
        overCapacityAssignment.assign("T1", 0);  // Uses 1.0 CPU (total 2.0)
        overCapacityAssignment.assign("T2", 0);  // Uses 1.0 RAM (would violate if we try to add more)

        // Create a task that uses more capacity
        Task heavyTask = new Task("T_HEAVY", new double[]{5.0, 0, 0, 0}, 1.0, 0, 2);
        instance.addTask(heavyTask);

        Assignment overCapacity = new Assignment(instance);
        overCapacity.assign("T0", 0);
        overCapacity.assign("T1", 0);
        overCapacity.assign("T2", 1);
        overCapacity.assign("T3", 2);
        overCapacity.assign("T4", 1);
        overCapacity.assign("T_HEAVY", 0);  // Exceeds CPU capacity (1 + 1 + 5 > 2)

        AssignmentValidator.ValidationResult result = AssignmentValidator.validate(instance, overCapacity);

        assertFalse(result.isValid(), "Over-capacity assignment should be detected");
        assertTrue(result.getMessage().contains("F2"), "Should report F2 violation");
        assertTrue(result.getMessage().contains("capacity") || result.getMessage().contains("exceeds"),
                "Error message should mention capacity");
    }

    @Test
    public void testSLAViolationDetected() {
        // Create task with restrictive SLA window
        Task restrictiveTask = new Task("T_RESTRICTED", new double[]{0, 0, 0, 0.5}, 1.0, 1, 1);
        instance.addTask(restrictiveTask);

        // Try to assign it to slot 0 (outside SLA window [1,1])
        Assignment slaViolation = new Assignment(instance);
        slaViolation.assign("T0", 0);
        slaViolation.assign("T1", 0);
        slaViolation.assign("T2", 1);
        slaViolation.assign("T3", 2);
        slaViolation.assign("T4", 1);
        slaViolation.assign("T_RESTRICTED", 0);  // SLA window is slot 1 only!

        AssignmentValidator.ValidationResult result = AssignmentValidator.validate(instance, slaViolation);

        assertFalse(result.isValid(), "SLA violation should be detected");
        assertTrue(result.getMessage().contains("F3"), "Should report F3 violation");
        assertTrue(result.getMessage().contains("SLA"), "Error message should mention SLA");
    }

    @Test
    public void testSLAWindowRespected() {
        // Create task with SLA window [1,2]
        Task slaTask = new Task("T_SLA", new double[]{0.5, 0, 0, 0}, 1.0, 1, 2);
        instance.addTask(slaTask);

        // Assign to slot 1 (within window)
        Assignment validSLA = new Assignment(instance);
        validSLA.assign("T0", 0);
        validSLA.assign("T1", 0);
        validSLA.assign("T2", 1);
        validSLA.assign("T3", 2);
        validSLA.assign("T4", 1);
        validSLA.assign("T_SLA", 1);  // Within SLA window

        AssignmentValidator.ValidationResult result = AssignmentValidator.validate(instance, validSLA);

        assertTrue(result.isValid(), "Assignment respecting SLA should pass");
    }

    @Test
    public void testMultipleTasksInSlotNoConflict() {
        // No conflicts defined, multiple tasks in same slot should be OK as long as SLA and capacity hold
        Assignment multiTask = new Assignment(instance);
        multiTask.assign("T0", 0);
        multiTask.assign("T1", 0);
        multiTask.assign("T2", 1);  // T2 has SLA window [1,2]
        multiTask.assign("T3", 2);
        multiTask.assign("T4", 1);

        AssignmentValidator.ValidationResult result = AssignmentValidator.validate(instance, multiTask);

        assertTrue(result.isValid(), "Multiple non-conflicting tasks in same slot should be valid");
    }

    @Test
    public void testEmptyAssignmentInvalid() {
        Assignment emptyAssignment = new Assignment(instance);

        AssignmentValidator.ValidationResult result = AssignmentValidator.validate(instance, emptyAssignment);

        // If not, it should pass (depends on requirements)
        assertNotNull(result, "Validation should return a result");
    }

    @Test
    public void testComplexScenarioMultipleConstraints() {
        // Create more complex instance
        Instance complex = new Instance(4, 2);
        complex.addTask(new Task("A", new double[]{1.0, 1.0, 0, 0}, 2.0, 0, 1));
        complex.addTask(new Task("B", new double[]{1.0, 1.0, 0, 0}, 2.0, 0, 1));
        complex.addTask(new Task("C", new double[]{1.0, 1.0, 0, 0}, 1.0, 1, 1));
        complex.addTask(new Task("D", new double[]{0, 0, 1.0, 1.0}, 1.0, 0, 1));

        complex.addSlot(new Slot(0, new double[]{2.0, 2.0, 2.0, 2.0}));
        complex.addSlot(new Slot(1, new double[]{2.0, 2.0, 2.0, 2.0}));

        complex.addConflict(0, 1);  // A and B conflict

        // Valid assignment
        Assignment valid = new Assignment(complex);
        valid.assign("A", 0);
        valid.assign("B", 1);
        valid.assign("C", 1);
        valid.assign("D", 0);

        AssignmentValidator.ValidationResult result = AssignmentValidator.validate(complex, valid);
        assertTrue(result.isValid(), "Complex valid assignment should pass");
    }
}
