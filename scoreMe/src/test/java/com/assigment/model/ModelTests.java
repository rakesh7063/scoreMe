package com.assigment.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelTests {
    @Test
    public void testTaskInitialization() {
        double[] resources = {1.5, 2.0, 0.5, 1.0};
        Task task = new Task("T1", resources, 2.5, 0, 3);

        assertEquals("T1", task.getId());
        assertEquals(2.5, task.getWeight());
        assertEquals(0, task.getSlaStart());
        assertEquals(3, task.getSlaEnd());

        double[] res = task.getResources();
        for (int i = 0; i < 4; i++) {
            assertEquals(resources[i], res[i], 1e-6);
        }
    }

    @Test
    public void testTaskSLAValidation() {
        Task task = new Task("T0", new double[]{1.0, 1.0, 1.0, 1.0}, 1.0, 1, 3);

        // SLA window is [1, 3]
        assertTrue(task.isSlaViolated(0), "Slot 0 should violate SLA [1,3]");
        assertFalse(task.isSlaViolated(1), "Slot 1 should be valid");
        assertTrue(task.isSlaViolated(4), "Slot 4 should violate SLA [1,3]");
    }

    @Test
    public void testTaskWithZeroWeight() {
        Task task = new Task("T_ZERO", new double[]{1.0, 0, 0, 0}, 0.0, 0, 5);

        assertEquals(0.0, task.getWeight());
        assertNotNull(task.getId());
    }

    @Test
    public void testSlotInitialization() {
        double[] capacity = {5.0, 5.0, 5.0, 5.0};
        Slot slot = new Slot(0, capacity);

        double[] cap = slot.getCapacity();
        for (int i = 0; i < 4; i++) {
            assertEquals(capacity[i], cap[i], 1e-6);
        }
    }

    @Test
    public void testSlotCapacityDimensions() {
        Slot slot = new Slot(1, new double[]{10.0, 20.0, 30.0, 40.0});

        double[] capacity = slot.getCapacity();
        assertEquals(4, capacity.length, "Slot should have capacity for 4 dimensions");
        assertEquals(10.0, capacity[0]);
        assertEquals(20.0, capacity[1]);
        assertEquals(30.0, capacity[2]);
        assertEquals(40.0, capacity[3]);
    }

    @Test
    public void testAssignmentInitialization() {
        Instance instance = new Instance(3, 2);
        instance.addTask(new Task("T0", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));
        instance.addTask(new Task("T1", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));
        instance.addTask(new Task("T2", new double[]{0, 1.0, 0, 0}, 1.0, 0, 1));

        Assignment assignment = new Assignment(instance);

        // All tasks should be unassigned initially
        assertEquals(-1, assignment.getSlot("T0"));
        assertEquals(-1, assignment.getSlot("T1"));
        assertEquals(-1, assignment.getSlot("T2"));
    }

    @Test
    public void testAssignmentOperations() {
        Instance instance = new Instance(2, 2);
        instance.addTask(new Task("T0", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));
        instance.addTask(new Task("T1", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));

        Assignment assignment = new Assignment(instance);

        assignment.assign("T0", 0);
        assertEquals(0, assignment.getSlot("T0"), "T0 should be in slot 0");

        assignment.assign("T1", 1);
        assertEquals(1, assignment.getSlot("T1"), "T1 should be in slot 1");

        assignment.assign("T0", 1);
        assertEquals(1, assignment.getSlot("T0"), "T0 should be reassigned to slot 1");
    }

    @Test
    public void testAssignmentCompleteness() {
        Instance instance = new Instance(2, 2);
        instance.addTask(new Task("T0", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));
        instance.addTask(new Task("T1", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));

        Assignment assignment = new Assignment(instance);

        assertFalse(assignment.isComplete(), "Unassigned tasks means incomplete");

        assignment.assign("T0", 0);
        assertFalse(assignment.isComplete(), "Still has unassigned task");

        assignment.assign("T1", 1);
        assertTrue(assignment.isComplete(), "All tasks assigned means complete");
    }

    @Test
    public void testAssignmentGetMap() {
        Instance instance = new Instance(2, 2);
        instance.addTask(new Task("T0", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));
        instance.addTask(new Task("T1", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));

        Assignment assignment = new Assignment(instance);
        assignment.assign("T0", 0);
        assignment.assign("T1", 1);

        java.util.Map<String, Integer> map = assignment.getAssignment();

        assertEquals(0, map.get("T0"));
        assertEquals(1, map.get("T1"));
    }

    @Test
    public void testResultFeasible() {
        Instance instance = new Instance(1, 1);
        instance.addTask(new Task("T0", new double[]{1.0, 0, 0, 0}, 1.0, 0, 0));
        instance.addSlot(new Slot(0, new double[]{5.0, 5.0, 5.0, 5.0}));

        Assignment assignment = new Assignment(instance);
        assignment.assign("T0", 0);

        Result result = Result.feasible(assignment, 42.0, 100);

        assertTrue(result.isFeasible());
        assertEquals(42.0, result.getPenalty(), 1e-6);
        assertEquals(100, result.getRuntimeMs());
        assertNull(result.getViolationReason());
    }

    @Test
    public void testResultInfeasible() {
        Result result = Result.infeasible("No valid slot found", 50);

        assertFalse(result.isFeasible());
        assertEquals(Double.MAX_VALUE, result.getPenalty());
        assertEquals(50, result.getRuntimeMs());
        assertEquals("No valid slot found", result.getViolationReason());
    }

    @Test
    public void testInstanceStructure() {
        Instance instance = new Instance(5, 3);

        assertEquals(5, instance.getN());
        assertEquals(3, instance.getK());
        assertEquals(0, instance.getTasks().size());
        assertEquals(0, instance.getSlots().size());
    }

    @Test
    public void testInstanceAddTask() {
        Instance instance = new Instance(3, 2);
        Task task1 = new Task("T0", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1);
        Task task2 = new Task("T1", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1);

        instance.addTask(task1);
        assertEquals(1, instance.getTasks().size());

        instance.addTask(task2);
        assertEquals(2, instance.getTasks().size());
    }

    @Test
    public void testInstanceAddSlot() {
        Instance instance = new Instance(3, 2);
        Slot slot1 = new Slot(0, new double[]{5.0, 5.0, 5.0, 5.0});
        Slot slot2 = new Slot(1, new double[]{5.0, 5.0, 5.0, 5.0});

        instance.addSlot(slot1);
        assertEquals(1, instance.getSlots().size());

        instance.addSlot(slot2);
        assertEquals(2, instance.getSlots().size());
    }

    @Test
    public void testInstanceConflictGraph() {
        Instance instance = new Instance(3, 2);
        instance.addTask(new Task("T0", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));
        instance.addTask(new Task("T1", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));
        instance.addTask(new Task("T2", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));

        ConflictGraph graph = instance.getConflictGraph();
        assertNotNull(graph);

        instance.addConflict(0, 1);
        java.util.Set<String> conflicts = graph.getConflicts(0);
        assertTrue(conflicts.contains("T1"), "T0 should conflict with T1");
    }

    @Test
    public void testConflictGraphSymmetry() {
        ConflictGraph graph = new ConflictGraph(3);

        graph.addConflict("T0", "T1");

        assertTrue(graph.getConflicts(0).contains("T1"));
        assertTrue(graph.getConflicts(1).contains("T0"));
    }

    @Test
    public void testConflictGraphHasConflict() {
        ConflictGraph graph = new ConflictGraph(3);
        graph.addConflict("T0", "T2");

        assertTrue(graph.hasConflict("T0", "T2"));
        assertTrue(graph.hasConflict("T2", "T0"));
        assertFalse(graph.hasConflict("T0", "T1"));
    }

    @Test
    public void testConflictGraphEdgeList() {
        ConflictGraph graph = new ConflictGraph(3);

        graph.addConflict("T0", "T1");
        graph.addConflict("T1", "T2");

        java.util.List<ConflictGraph.Pair<Integer, Integer>> edges = graph.getEdges();

        assertEquals(2, edges.size(), "Should have 2 edges");
    }

    @Test
    public void testAssignmentFromMap() {
        Instance instance = new Instance(2, 2);
        instance.addTask(new Task("T0", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));
        instance.addTask(new Task("T1", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));

        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        map.put("T0", 0);
        map.put("T1", 1);

        Assignment assignment = Assignment.fromMap(instance, map);

        assertEquals(0, assignment.getSlot("T0"));
        assertEquals(1, assignment.getSlot("T1"));
    }

    @Test
    public void testResultToString() {
        Result result = Result.infeasible("Test violation", 25);

        String str = result.toString();
        assertTrue(str.contains("feasible=false"));
        assertTrue(str.contains("penalty"));
        assertTrue(str.contains("runtimeMs=25"));
    }

    @Test
    public void testTaskToString() {
        Task task = new Task("T_TEST", new double[]{1.0, 2.0, 3.0, 4.0}, 5.0, 0, 10);

        String str = task.toString();
        assertNotNull(str);
        assertTrue(str.contains("T_TEST") || str.contains("Task"));
    }
}
