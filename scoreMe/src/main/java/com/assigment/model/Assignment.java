package com.assigment.model;

import java.util.HashMap;
import java.util.Map;

public class Assignment {
    private Map<String, Integer> assignment;
    private Instance instance;

    public Assignment(Instance instance) {
        this.instance = instance;
        this.assignment = new HashMap<>();
        // Initialize all tasks as unassigned (-1)
        for (Task task : instance.getTasks()) {
            assignment.put(task.getId(), -1);
        }
    }

    public Assignment(Instance instance, Map<String, Integer> assignmentMap) {
        this.instance = instance;
        this.assignment = new HashMap<>();
        for (Task task : instance.getTasks()) {
            int slot = assignmentMap.getOrDefault(task.getId(), -1);
            assignment.put(task.getId(), slot);
        }
    }

    public static Assignment fromMap(Instance instance, Map<String, Integer> assignmentMap) {
        return new Assignment(instance, assignmentMap);
    }

    public void assign(String taskId, int slotIndex) {
        assignment.put(taskId, slotIndex);
    }

    public int getSlot(String taskId) {
        return assignment.getOrDefault(taskId, -1);
    }

    public Map<String, Integer> getAssignment() {
        return new HashMap<>(assignment);
    }

    public boolean isComplete() {
        for (int slot : assignment.values()) {
            if (slot == -1) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Assignment{\n");
        for (Map.Entry<String, Integer> entry : assignment.entrySet()) {
            sb.append(entry.getKey()).append(" -> Slot ").append(entry.getValue()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
