package com.assigment.model;

import java.util.ArrayList;
import java.util.List;

public class Slot {
    private int index;
    private double[] capacity;  // [CPU_cores, RAM_GB, GPU_units, Net_Gbps]
    private List<String> assignedTasks;

    public Slot(int index, double[] capacity) {
        this.index = index;
        this.capacity = capacity.clone();
        this.assignedTasks = new ArrayList<>();
    }

    public int getIndex() {
        return index;
    }

    public double[] getCapacity() {
        return capacity;
    }

    public void assignTask(String taskId) {
        assignedTasks.add(taskId);
    }

    public void removeTask(String taskId) {
        assignedTasks.remove(taskId);
    }

    public List<String> getAssignedTasks() {
        return new ArrayList<>(assignedTasks);
    }

    public boolean canAccommodate(double[] resourceRequirement) {
        for (int i = 0; i < 4; i++) {
            if (resourceRequirement[i] > capacity[i]) {
                return false;
            }
        }
        return true;
    }

    public double[] getAvailableCapacity(double[] usedCapacity) {
        double[] available = new double[4];
        for (int i = 0; i < 4; i++) {
            available[i] = capacity[i] - usedCapacity[i];
        }
        return available;
    }

    @Override
    public String toString() {
        return "Slot{" + "index=" + index + ", tasks=" + assignedTasks.size() + '}';
    }
}
