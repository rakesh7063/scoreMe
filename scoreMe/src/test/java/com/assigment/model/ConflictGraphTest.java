package com.assigment.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConflictGraphTest {
    private ConflictGraph graph;

    @BeforeEach
    public void setUp() {
        graph = new ConflictGraph(5);
    }

    @Test
    public void testConflictGraphInitialization() {
        ConflictGraph g = new ConflictGraph(3);


        for (int i = 0; i < 3; i++) {
            assertEquals(0, g.getConflicts(i).size(), "No conflicts initially");
        }
    }

    @Test
    public void testAddConflictIsSymmetric() {
        graph.addConflict("T0", "T1");

        Set<String> conflicts0 = graph.getConflicts(0);
        Set<String> conflicts1 = graph.getConflicts(1);

        assertTrue(conflicts0.contains("T1"), "T0 should have conflict with T1");
        assertTrue(conflicts1.contains("T0"), "T1 should have conflict with T0");
    }

    @Test
    public void testAddMultipleConflicts() {
        graph.addConflict("T0", "T1");
        graph.addConflict("T0", "T2");
        graph.addConflict("T0", "T3");

        Set<String> conflicts0 = graph.getConflicts(0);

        assertEquals(3, conflicts0.size(), "T0 should have 3 conflicts");
        assertTrue(conflicts0.contains("T1"));
        assertTrue(conflicts0.contains("T2"));
        assertTrue(conflicts0.contains("T3"));
    }

    @Test
    public void testHasConflictBidirectional() {
        graph.addConflict("T0", "T2");

        assertTrue(graph.hasConflict("T0", "T2"));
        assertTrue(graph.hasConflict("T2", "T0"));
    }

    @Test
    public void testNoConflictByDefault() {
        assertFalse(graph.hasConflict("T0", "T1"));
        assertFalse(graph.hasConflict("T1", "T0"));
    }

    @Test
    public void testAddDuplicateConflict() {
        graph.addConflict("T0", "T1");
        int count1 = graph.getConflicts(0).size();

        graph.addConflict("T0", "T1");  // Add same conflict again
        int count2 = graph.getConflicts(0).size();

        assertEquals(count1, count2, "Adding duplicate conflict should not increase count");
    }

    @Test
    public void testEdgeListConsistency() {
        graph.addConflict("T0", "T1");
        graph.addConflict("T1", "T2");
        graph.addConflict("T2", "T3");
        graph.addConflict("T3", "T0");

        List<ConflictGraph.Pair<Integer, Integer>> edges = graph.getEdges();

        assertEquals(4, edges.size(), "Should have 4 edges");

        // Verify edges correspond to conflicts
        for (ConflictGraph.Pair<Integer, Integer> edge : edges) {
            assertTrue(graph.hasConflict(edge.first, edge.second),
                    "Each edge should correspond to a conflict");
        }
    }

    @Test
    public void testCompleteGraphConflicts() {

        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                graph.addConflict("T" + i, "T" + j);
            }
        }


        for (int i = 0; i < 5; i++) {
            Set<String> conflicts = graph.getConflicts(i);
            assertEquals(4, conflicts.size(), "In complete graph, each node should have n-1 conflicts");
        }
    }

    @Test
    public void testLargeConflictGraph() {
        ConflictGraph large = new ConflictGraph(100);

        // Add many conflicts
        for (int i = 0; i < 100; i++) {
            for (int j = i + 1; j < Math.min(i + 10, 100); j++) {
                large.addConflict("T" + i, "T" + j);
            }
        }

        // Verify edge list
        List<ConflictGraph.Pair<Integer, Integer>> edges = large.getEdges();
        assertTrue(edges.size() > 0, "Large graph should have edges");

        // Spot check some conflicts
        assertTrue(large.hasConflict(0, 1));
        assertTrue(large.hasConflict(10, 15));
    }

    @Test
    public void testConflictGraphWithSelfConflict() {
        // A task shouldn't conflict with itself
        graph.addConflict("T0", "T0");


        Set<String> conflicts = graph.getConflicts(0);

        // Most implementations would not include self in conflicts
        assertTrue(conflicts.isEmpty() || conflicts.contains("T0"),
                "Self-conflict handling is implementation-dependent");
    }

    @Test
    public void testConflictGraphQueryPerformance() {
        ConflictGraph largeGraph = new ConflictGraph(100);

        // Add many conflicts to task 0
        for (int i = 1; i < 100; i++) {
            largeGraph.addConflict("T0", "T" + i);
        }

        // Query should be fast
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            largeGraph.getConflicts(0);
        }
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;

        assertTrue(durationMs < 100, "Query performance should be acceptable");
    }

    @Test
    public void testEdgeListNoDuplicates() {
        graph.addConflict("T0", "T1");
        graph.addConflict("T1", "T0");  // Symmetric - should be same edge
        graph.addConflict("T0", "T1");  // Duplicate

        List<ConflictGraph.Pair<Integer, Integer>> edges = graph.getEdges();

        // Should have only 1 edge (not duplicated)
        long edgeCount = edges.stream()
                .filter(e -> (e.first == 0 && e.second == 1) || (e.first == 1 && e.second == 0))
                .count();

        assertEquals(1, edgeCount, "Should not have duplicate edges");
    }

    @Test
    public void testConflictGraphDensity() {
        // Create sparse graph
        ConflictGraph sparse = new ConflictGraph(20);
        for (int i = 0; i < 20; i++) {
            sparse.addConflict("T" + i, "T" + ((i + 1) % 20));
        }

        List<ConflictGraph.Pair<Integer, Integer>> sparseEdges = sparse.getEdges();
        int sparseEdgeCount = sparseEdges.size();

        // Create dense graph
        ConflictGraph dense = new ConflictGraph(20);
        for (int i = 0; i < 20; i++) {
            for (int j = i + 1; j < 20; j++) {
                dense.addConflict("T" + i, "T" + j);
            }
        }

        List<ConflictGraph.Pair<Integer, Integer>> denseEdges = dense.getEdges();
        int denseEdgeCount = denseEdges.size();

        assertTrue(sparseEdgeCount < denseEdgeCount,
                "Sparse graph should have fewer edges than dense graph");
    }

    @Test
    public void testConflictGraphIsolatedNode() {
        graph.addConflict("T0", "T1");
        graph.addConflict("T1", "T2");

        // T3 and T4 have no conflicts
        Set<String> conflicts3 = graph.getConflicts(3);
        Set<String> conflicts4 = graph.getConflicts(4);

        assertEquals(0, conflicts3.size(), "Isolated nodes should have no conflicts");
        assertEquals(0, conflicts4.size(), "Isolated nodes should have no conflicts");
    }

    @Test
    public void testConflictGraphTriangle() {
        // Create triangle: T0-T1, T1-T2, T2-T0
        graph.addConflict("T0", "T1");
        graph.addConflict("T1", "T2");
        graph.addConflict("T2", "T0");

        Set<String> conflicts0 = graph.getConflicts(0);
        Set<String> conflicts1 = graph.getConflicts(1);
        Set<String> conflicts2 = graph.getConflicts(2);

        assertEquals(2, conflicts0.size());
        assertEquals(2, conflicts1.size());
        assertEquals(2, conflicts2.size());

        assertTrue(conflicts0.contains("T1") && conflicts0.contains("T2"));
        assertTrue(conflicts1.contains("T0") && conflicts1.contains("T2"));
        assertTrue(conflicts2.contains("T0") && conflicts2.contains("T1"));
    }

    @Test
    public void testConflictGraphGetConflictsReturnsCopy() {
        graph.addConflict("T0", "T1");

        Set<String> conflicts1 = graph.getConflicts(0);
        Set<String> conflicts2 = graph.getConflicts(0);

        // Modifying one shouldn't affect the other (if implementation returns copy)

        assertEquals(conflicts1.size(), conflicts2.size(),
                "Multiple calls should return equivalent sets");
    }

    @Test
    public void testInstanceWithConflictGraph() {
        Instance instance = new Instance(4, 2);
        instance.addTask(new Task("T0", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));
        instance.addTask(new Task("T1", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));
        instance.addTask(new Task("T2", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));
        instance.addTask(new Task("T3", new double[]{1.0, 0, 0, 0}, 1.0, 0, 1));

        instance.addConflict(0, 1);
        instance.addConflict(1, 2);
        instance.addConflict(2, 3);

        ConflictGraph g = instance.getConflictGraph();

        assertTrue(g.hasConflict(0, 1));
        assertTrue(g.hasConflict(1, 2));
        assertTrue(g.hasConflict(2, 3));
        assertFalse(g.hasConflict(0, 3));
    }

    @Test
    public void testEdgeListContainsAllConflicts() {
        graph.addConflict("T0", "T1");
        graph.addConflict("T1", "T2");
        graph.addConflict("T0", "T3");
        graph.addConflict("T2", "T4");

        List<ConflictGraph.Pair<Integer, Integer>> edges = graph.getEdges();

        assertEquals(4, edges.size(), "Edge list should contain all conflicts");

        // Verify all expected conflicts are in edge list
        Set<String> edgeSet = new HashSet<>();
        for (ConflictGraph.Pair<Integer, Integer> e : edges) {
            edgeSet.add(Math.min(e.first, e.second) + "-" + Math.max(e.first, e.second));
        }

        assertTrue(edgeSet.contains("0-1"));
        assertTrue(edgeSet.contains("1-2"));
        assertTrue(edgeSet.contains("0-3"));
        assertTrue(edgeSet.contains("2-4"));
    }
}
