package com.assigment.model;

public class Task {
    private String id;
    private double[] resources;  // [CPU, RAM, GPU, Net]
    private double weight;
    private int slaStart;
    private int slaEnd;

    public Task(String id, double[] resources, double weight, int slaStart, int slaEnd) {
        this.id = id;
        this.resources = resources;
        this.weight = weight;
        this.slaStart = slaStart;
        this.slaEnd = slaEnd;
    }

    public String getId() {
        return id;
    }

    public double[] getResources() {
        return resources;
    }

    public double getWeight() {
        return weight;
    }

    public int getSlaStart() {
        return slaStart;
    }

    public int getSlaEnd() {
        return slaEnd;
    }

    public boolean isSlaViolated(int slotIndex) {
        return slotIndex < slaStart || slotIndex > slaEnd;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", weight=" + weight +
                ", sla=[" + slaStart + "," + slaEnd + "]" +
                '}';
    }
}
