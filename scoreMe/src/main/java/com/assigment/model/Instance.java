package com.assigment.model;

import java.util.ArrayList;
import java.util.List;

public class Instance {
    private List<Task> tasks;
    private List<Slot> slots;
    private ConflictGraph conflictGraph;
    private int K;  // number of slots
    private int n;  // number of tasks

    public Instance(int n, int K) {
        this.n = n;
        this.K = K;
        this.tasks = new ArrayList<>();
        this.slots = new ArrayList<>();
        this.conflictGraph = new ConflictGraph(n);
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void addSlot(Slot slot) {
        slots.add(slot);
    }

    public void addConflict(int taskI, int taskJ) {
        conflictGraph.addConflict(taskI, taskJ);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public ConflictGraph getConflictGraph() {
        return conflictGraph;
    }

    public int getK() {
        return K;
    }

    public int getN() {
        return n;
    }

    public Task getTask(int index) {
        return tasks.get(index);
    }

    public Slot getSlot(int index) {
        return slots.get(index);
    }

    @Override
    public String toString() {
        return "Instance{" +
                "n=" + n +
                ", K=" + K +
                ", conflicts=" + conflictGraph.getTotalEdges() +
                ", density=" + String.format("%.2f", conflictGraph.getDensity()) +
                '}';
    }
}
