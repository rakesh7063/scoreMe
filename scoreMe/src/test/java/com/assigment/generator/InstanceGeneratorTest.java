package com.assigment.generator;

import com.assigment.model.ConflictGraph;
import com.assigment.model.Instance;
import com.assigment.model.Slot;
import com.assigment.model.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InstanceGeneratorTest {
    @Test
    public void testGenerateInstanceHasValidStructure() {
        // Generate a small instance
        Instance instance = InstanceGenerator.generateInstance(10, 3, 0.1, 42L);

        // Verify basic properties
        assertEquals(10, instance.getN(), "Instance should have n=10 tasks");
        assertEquals(3, instance.getK(), "Instance should have K=3 slots");
        assertEquals(10, instance.getTasks().size(), "Should have 10 tasks");
        assertEquals(3, instance.getSlots().size(), "Should have 3 slots");
    }

    @Test
    public void testGenerateInstanceTasksHaveResources() {
        Instance instance = InstanceGenerator.generateInstance(5, 2, 0.1, 123L);

        for (Task task : instance.getTasks()) {
            assertNotNull(task.getId(), "Task should have an ID");
            assertNotNull(task.getResources(), "Task should have resources");
            assertEquals(4, task.getResources().length, "Task should have 4 resource dimensions");

            for (double resource : task.getResources()) {
                assertTrue(resource >= 0, "Resources should be non-negative");
            }

            assertTrue(task.getWeight() > 0, "Task weight should be positive");
            assertTrue(task.getSlaStart() >= 0, "SLA start should be non-negative");
            assertTrue(task.getSlaEnd() >= task.getSlaStart(), "SLA end should be >= start");
        }
    }

    @Test
    public void testGenerateInstanceSlotsHaveCapacity() {
        Instance instance = InstanceGenerator.generateInstance(5, 3, 0.1, 456L);

        for (Slot slot : instance.getSlots()) {
            assertNotNull(slot.getCapacity(), "Slot should have capacity");
            assertEquals(4, slot.getCapacity().length, "Slot capacity should have 4 dimensions");

            for (double cap : slot.getCapacity()) {
                assertTrue(cap > 0, "Slot capacity should be positive");
            }
        }
    }

    @Test
    public void testGenerateInstanceConflictGraphConsistency() {
        Instance instance = InstanceGenerator.generateInstance(8, 2, 0.3, 789L);
        ConflictGraph graph = instance.getConflictGraph();

        // Verify conflicts are symmetric
        for (int i = 0; i < instance.getN(); i++) {
            for (String conflictId : graph.getConflicts(i)) {
                int j = Integer.parseInt(conflictId.substring(1));
                String inverseId = "T" + i;
                assertTrue(graph.getConflicts(j).contains(inverseId),
                        "Conflict should be symmetric: if T" + i + " conflicts with " + conflictId +
                                ", then " + conflictId + " should conflict with T" + i);
            }
        }
    }

    @Test
    public void testBenchmarkSuiteHasNineConfigs() {
        List<InstanceGenerator.BenchmarkConfig> benchmarks = InstanceGenerator.getBenchmarkSuite();

        assertEquals(9, benchmarks.size(), "Benchmark suite should have 9 configurations");

        // Verify benchmark sizes increase
        int prevN = 0;
        for (InstanceGenerator.BenchmarkConfig config : benchmarks) {
            assertNotNull(config.name, "Benchmark should have a name");
            assertTrue(config.n >= prevN, "Benchmark instances should be ordered by size");
            assertTrue(config.K > 0, "K should be positive");
            prevN = config.n;
        }
    }

    @Test
    public void testBenchmarkConfigsAreValid() {
        List<InstanceGenerator.BenchmarkConfig> benchmarks = InstanceGenerator.getBenchmarkSuite();

        String[] expectedNames = {
                "small_8_3", "small_10_4", "small_12_4",
                "medium_50_8", "medium_100_10", "medium_150_12",
                "stress_200_15", "stress_200_5_tight", "stress_200_20_sparse"
        };

        for (int i = 0; i < benchmarks.size(); i++) {
            assertEquals(expectedNames[i], benchmarks.get(i).name,
                    "Benchmark " + i + " should have expected name");
        }
    }

    @Test
    public void testGenerateInstanceReproducibility() {
        // Generate two instances with same seed
        Instance inst1 = InstanceGenerator.generateInstance(10, 3, 0.1, 999L);
        Instance inst2 = InstanceGenerator.generateInstance(10, 3, 0.1, 999L);

        // Verify they have same number of conflicts
        assertEquals(
                inst1.getConflictGraph().getEdges().size(),
                inst2.getConflictGraph().getEdges().size(),
                "Instances with same seed should generate same number of conflicts"
        );

        // Verify tasks have same IDs (order)
        List<Task> tasks1 = new ArrayList<>(inst1.getTasks());
        List<Task> tasks2 = new ArrayList<>(inst2.getTasks());
        for (int i = 0; i < tasks1.size(); i++) {
            assertEquals(tasks1.get(i).getId(), tasks2.get(i).getId(),
                    "Tasks should be in same order with same seed");
        }
    }

    @Test
    public void testGenerateInstanceDifferentSeedsProduceDifferentInstances() {
        Instance inst1 = InstanceGenerator.generateInstance(10, 3, 0.1, 111L);
        Instance inst2 = InstanceGenerator.generateInstance(10, 3, 0.1, 222L);

        // Should have different number of conflicts (very likely)
        int conflicts1 = inst1.getConflictGraph().getEdges().size();
        int conflicts2 = inst2.getConflictGraph().getEdges().size();


        // This test documents expected behavior but won't fail if they happen to be same
        assertTrue(conflicts1 != conflicts2 || inst1.getTasks().get(0).getWeight() != inst2.getTasks().get(0).getWeight(),
                "Different seeds should (very likely) produce different instances");
    }

    @Test
    public void testGenerateInstanceDensityParameter() {
        // Low density should have fewer conflicts
        Instance sparse = InstanceGenerator.generateInstance(20, 3, 0.05, 100L);
        Instance dense = InstanceGenerator.generateInstance(20, 3, 0.5, 100L);

        int sparseConflicts = sparse.getConflictGraph().getEdges().size();
        int denseConflicts = dense.getConflictGraph().getEdges().size();

        assertTrue(sparseConflicts <= denseConflicts,
                "Sparse instance (density=0.05) should have <= conflicts than dense (density=0.5)");
    }
}
