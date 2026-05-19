package com.assigment.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ConflictGraph {
    private int numTasks;
    private Set<String>[] adjacencyList;
    private Set<Pair<Integer, Integer>> edges;

    @SuppressWarnings("unchecked")
    public ConflictGraph(int numTasks) {
        this.numTasks = numTasks;
        this.adjacencyList = new HashSet[numTasks];
        for (int i = 0; i < numTasks; i++) {
            this.adjacencyList[i] = new HashSet<>();
        }
        this.edges = new HashSet<>();
    }

    public void addConflict(int taskI, int taskJ) {
        if (taskI != taskJ) {
            adjacencyList[taskI].add("T" + taskJ);
            adjacencyList[taskJ].add("T" + taskI);
            int min = Math.min(taskI, taskJ);
            int max = Math.max(taskI, taskJ);
            edges.add(new Pair<>(min, max));
        }
    }

    public void addConflict(String taskI, String taskJ) {
        addConflict(parseTaskId(taskI), parseTaskId(taskJ));
    }

    public boolean hasConflict(int taskI, int taskJ) {
        if (taskI < 0 || taskI >= numTasks || taskJ < 0 || taskJ >= numTasks) {
            return false;
        }
        return adjacencyList[taskI].contains("T" + taskJ);
    }

    public boolean hasConflict(String taskI, String taskJ) {
        return hasConflict(parseTaskId(taskI), parseTaskId(taskJ));
    }

    private int parseTaskId(String taskId) {
        if (taskId == null || !taskId.startsWith("T")) {
            throw new IllegalArgumentException("Invalid task id: " + taskId);
        }
        return Integer.parseInt(taskId.substring(1));
    }

    public Set<String> getConflicts(int taskIndex) {
        return new HashSet<>(adjacencyList[taskIndex]);
    }

    public int getConflictCount(int taskIndex) {
        return adjacencyList[taskIndex].size();
    }

    public int getTotalEdges() {
        return edges.size();
    }

    public java.util.List<Pair<Integer, Integer>> getEdges() {
        return new java.util.ArrayList<>(edges);
    }

    public double getDensity() {
        int maxEdges = numTasks * (numTasks - 1) / 2;
        return maxEdges > 0 ? (double) edges.size() / maxEdges : 0;
    }

    public int getChromaticLowerBound() {
        // Simple lower bound: max degree + 1
        int maxDegree = 0;
        for (int i = 0; i < numTasks; i++) {
            maxDegree = Math.max(maxDegree, adjacencyList[i].size());
        }
        return maxDegree + 1;
    }

    /**
     * Simple pair class for storing edges
     */
    public static class Pair<A, B> {
        public A first;
        public B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair)) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
}
