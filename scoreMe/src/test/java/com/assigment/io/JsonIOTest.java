package com.assigment.io;

import com.assigment.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonIOTest {
    @TempDir
    Path tempDir;

    private Instance testInstance;

    @BeforeEach
    public void setUp() {
        testInstance = new Instance(3, 2);
        testInstance.addTask(new Task("T0", new double[]{1.0, 0.5, 0.2, 0.1}, 2.5, 0, 1));
        testInstance.addTask(new Task("T1", new double[]{0.5, 1.0, 0.3, 0.2}, 1.5, 0, 1));
        testInstance.addTask(new Task("T2", new double[]{0.2, 0.3, 1.0, 0.5}, 3.0, 1, 1));

        testInstance.addSlot(new Slot(0, new double[]{5.0, 5.0, 5.0, 5.0}));
        testInstance.addSlot(new Slot(1, new double[]{5.0, 5.0, 5.0, 5.0}));

        testInstance.addConflict(0, 1);  // T0 conflicts with T1
    }

    @Test
    public void testRoundTripInstancePreservesData() throws IOException {
        Path instanceFile = tempDir.resolve("test_instance.json");

        // Save instance
        JsonIO.saveInstance(testInstance, instanceFile.toString());

        // Load instance
        Instance loaded = JsonIO.loadInstance(instanceFile.toString());

        // Verify basic properties
        assertEquals(testInstance.getN(), loaded.getN(), "N should be preserved");
        assertEquals(testInstance.getK(), loaded.getK(), "K should be preserved");
        assertEquals(testInstance.getTasks().size(), loaded.getTasks().size(), "Task count should be preserved");
        assertEquals(testInstance.getSlots().size(), loaded.getSlots().size(), "Slot count should be preserved");
    }

    @Test
    public void testRoundTripInstancePreservesTaskData() throws IOException {
        Path instanceFile = tempDir.resolve("test_instance.json");

        JsonIO.saveInstance(testInstance, instanceFile.toString());
        Instance loaded = JsonIO.loadInstance(instanceFile.toString());

        java.util.List<Task> originalTasks = new java.util.ArrayList<>(testInstance.getTasks());
        java.util.List<Task> loadedTasks = new java.util.ArrayList<>(loaded.getTasks());

        for (int i = 0; i < originalTasks.size(); i++) {
            Task orig = originalTasks.get(i);
            Task load = loadedTasks.get(i);

            assertEquals(orig.getId(), load.getId(), "Task ID should match");
            assertEquals(orig.getWeight(), load.getWeight(), "Task weight should match");
            assertEquals(orig.getSlaStart(), load.getSlaStart(), "SLA start should match");
            assertEquals(orig.getSlaEnd(), load.getSlaEnd(), "SLA end should match");

            double[] origRes = orig.getResources();
            double[] loadRes = load.getResources();
            for (int d = 0; d < 4; d++) {
                assertEquals(origRes[d], loadRes[d], 1e-6, "Resource dimension " + d + " should match");
            }
        }
    }

    @Test
    public void testRoundTripInstancePreservesSlotCapacity() throws IOException {
        Path instanceFile = tempDir.resolve("test_instance.json");

        JsonIO.saveInstance(testInstance, instanceFile.toString());
        Instance loaded = JsonIO.loadInstance(instanceFile.toString());

        java.util.List<Slot> originalSlots = testInstance.getSlots();
        java.util.List<Slot> loadedSlots = loaded.getSlots();

        for (int i = 0; i < originalSlots.size(); i++) {
            double[] origCap = originalSlots.get(i).getCapacity();
            double[] loadCap = loadedSlots.get(i).getCapacity();

            for (int d = 0; d < 4; d++) {
                assertEquals(origCap[d], loadCap[d], 1e-6, "Slot " + i + " capacity dimension " + d + " should match");
            }
        }
    }

    @Test
    public void testRoundTripInstancePreservesConflicts() throws IOException {
        Path instanceFile = tempDir.resolve("test_instance.json");

        JsonIO.saveInstance(testInstance, instanceFile.toString());
        Instance loaded = JsonIO.loadInstance(instanceFile.toString());

        // Verify conflict exists in loaded instance
        ConflictGraph origGraph = testInstance.getConflictGraph();
        ConflictGraph loadGraph = loaded.getConflictGraph();

        java.util.Set<String> origConflicts = origGraph.getConflicts(0);
        java.util.Set<String> loadConflicts = loadGraph.getConflicts(0);

        assertEquals(origConflicts.size(), loadConflicts.size(), "Conflict count should match");

        for (String conflict : origConflicts) {
            assertTrue(loadConflicts.contains(conflict), "Conflict " + conflict + " should be preserved");
        }
    }

    @Test
    public void testSaveAndLoadResultFeasible() throws IOException {
        Path resultFile = tempDir.resolve("test_result.json");

        // Create a feasible result
        Assignment assignment = new Assignment(testInstance);
        assignment.assign("T0", 0);
        assignment.assign("T1", 1);
        assignment.assign("T2", 1);

        Result original = Result.feasible(assignment, 123.45, 50);

        // Save result
        JsonIO.saveResult(original, resultFile.toString());

        // Load result
        Result loaded = JsonIO.loadResult(resultFile.toString());

        assertEquals(original.isFeasible(), loaded.isFeasible(), "Feasibility flag should match");
        assertEquals(original.getPenalty(), loaded.getPenalty(), 1e-6, "Penalty should match");
        assertEquals(original.getRuntimeMs(), loaded.getRuntimeMs(), "Runtime should match");
    }

    @Test
    public void testSaveAndLoadResultInfeasible() throws IOException {
        Path resultFile = tempDir.resolve("test_result_infeasible.json");

        // Create an infeasible result
        Result original = Result.infeasible("No valid assignment found", 100);

        // Save result
        JsonIO.saveResult(original, resultFile.toString());

        // Load result
        Result loaded = JsonIO.loadResult(resultFile.toString());

        assertFalse(loaded.isFeasible(), "Result should be marked infeasible");
        assertEquals(Double.MAX_VALUE, loaded.getPenalty(), "Infeasible penalty should be MAX_VALUE");
        assertEquals(100, loaded.getRuntimeMs(), "Runtime should match");
        assertNotNull(loaded.getViolationReason(), "Violation reason should be present");
    }

    @Test
    public void testLoadAssignmentWithInstanceContext() throws IOException {
        Path resultFile = tempDir.resolve("test_result_with_assignment.json");

        // Create and save a feasible result
        Assignment original = new Assignment(testInstance);
        original.assign("T0", 0);
        original.assign("T1", 1);
        original.assign("T2", 1);

        Result result = Result.feasible(original, 42.0, 10);
        JsonIO.saveResult(result, resultFile.toString());

        // Load assignment with instance context
        Assignment loaded = JsonIO.loadAssignment(testInstance, resultFile.toString());

        assertNotNull(loaded, "Assignment should be loaded");
        assertEquals(original.getSlot("T0"), loaded.getSlot("T0"), "T0 slot should match");
        assertEquals(original.getSlot("T1"), loaded.getSlot("T1"), "T1 slot should match");
        assertEquals(original.getSlot("T2"), loaded.getSlot("T2"), "T2 slot should match");
    }

    @Test
    public void testLoadAssignmentFromInfeasibleResultReturnsNull() throws IOException {
        Path resultFile = tempDir.resolve("test_infeasible_result.json");

        Result infeasible = Result.infeasible("Test infeasibility", 50);
        JsonIO.saveResult(infeasible, resultFile.toString());

        Assignment loaded = JsonIO.loadAssignment(testInstance, resultFile.toString());

        assertNull(loaded, "Assignment should be null for infeasible result");
    }

    @Test
    public void testFileNotFoundThrowsException() {
        assertThrows(IOException.class, () -> {
            JsonIO.loadInstance("nonexistent_file.json");
        }, "Should throw IOException for missing file");
    }

    @Test
    public void testComplexInstanceRoundTrip() throws IOException {
        Path instanceFile = tempDir.resolve("complex_instance.json");

        // Create a more complex instance
        Instance complex = new Instance(10, 5);
        for (int i = 0; i < 10; i++) {
            double[] res = new double[]{
                    Math.random(), Math.random(), Math.random(), Math.random()
            };
            Task task = new Task("Task" + i, res, Math.random() * 5, i % 5, (i + 2) % 5);
            complex.addTask(task);
        }

        for (int i = 0; i < 5; i++) {
            complex.addSlot(new Slot(i, new double[]{10.0, 10.0, 10.0, 10.0}));
        }

        for (int i = 0; i < 10; i++) {
            for (int j = i + 1; j < 10; j++) {
                if (Math.random() < 0.2) {
                    complex.addConflict(i, j);
                }
            }
        }

        // Save and load
        JsonIO.saveInstance(complex, instanceFile.toString());
        Instance loaded = JsonIO.loadInstance(instanceFile.toString());

        assertEquals(complex.getN(), loaded.getN());
        assertEquals(complex.getK(), loaded.getK());
        assertEquals(complex.getTasks().size(), loaded.getTasks().size());
        assertEquals(complex.getSlots().size(), loaded.getSlots().size());
    }

    @Test
    public void testResultMetadataPreserved() throws IOException {
        Path resultFile = tempDir.resolve("result_metadata.json");

        Assignment assignment = new Assignment(testInstance);
        assignment.assign("T0", 0);
        assignment.assign("T1", 1);
        assignment.assign("T2", 1);

        Result original = Result.feasible(assignment, 789.456, 12345);

        JsonIO.saveResult(original, resultFile.toString());
        Result loaded = JsonIO.loadResult(resultFile.toString());

        assertEquals(789.456, loaded.getPenalty(), 1e-3);
        assertEquals(12345, loaded.getRuntimeMs());
        assertTrue(loaded.isFeasible());
    }
}
