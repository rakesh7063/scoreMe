package com.assigment.validator;

import com.assigment.model.Assignment;
import com.assigment.model.ConflictGraph;
import com.assigment.model.Instance;
import com.assigment.model.Task;

import java.util.ArrayList;
import java.util.List;

public class AssignmentValidator {
    public static ValidationResult validate(Instance instance, Assignment assignment) {

        String f1Error = checkNoConflicts(instance, assignment);
        if (f1Error != null) {
            return ValidationResult.invalid("F1 violation: " + f1Error);
        }


        String f2Error = checkCapacity(instance, assignment);
        if (f2Error != null) {
            return ValidationResult.invalid("F2 violation: " + f2Error);
        }


        String f3Error = checkSLA(instance, assignment);
        if (f3Error != null) {
            return ValidationResult.invalid("F3 violation: " + f3Error);
        }

        return ValidationResult.valid();
    }

//     Check F1: No two conflicting tasks in same slot

    private static String checkNoConflicts(Instance instance, Assignment assignment) {
        ConflictGraph graph = instance.getConflictGraph();

        for (int s = 0; s < instance.getK(); s++) {
            List<String> tasksInSlot = new ArrayList<>();


            for (Task task : instance.getTasks()) {
                if (assignment.getSlot(task.getId()) == s) {
                    tasksInSlot.add(task.getId());
                }
            }

            // Check all pairs for conflicts
            for (int i = 0; i < tasksInSlot.size(); i++) {
                for (int j = i + 1; j < tasksInSlot.size(); j++) {
                    String t1 = tasksInSlot.get(i);
                    String t2 = tasksInSlot.get(j);

                    int idx1 = getTaskIndex(instance, t1);
                    int idx2 = getTaskIndex(instance, t2);

                    if (graph.hasConflict(idx1, idx2)) {
                        return "Conflicting tasks " + t1 + " and " + t2 + " in slot " + s;
                    }
                }
            }
        }

        return null;
    }

    private static int getTaskIndex(Instance instance, String taskId) {
        for (int i = 0; i < instance.getTasks().size(); i++) {
            if (instance.getTasks().get(i).getId().equals(taskId)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown task id: " + taskId);
    }


//     Check F2: Slot capacity not exceeded

    private static String checkCapacity(Instance instance, Assignment assignment) {
        for (int s = 0; s < instance.getK(); s++) {
            double[] used = new double[4];
            double[] capacity = instance.getSlot(s).getCapacity();


            for (Task task : instance.getTasks()) {
                if (assignment.getSlot(task.getId()) == s) {
                    for (int d = 0; d < 4; d++) {
                        used[d] += task.getResources()[d];
                    }
                }
            }

            // Check each dimension
            String[] dims = {"CPU", "RAM", "GPU", "Network"};
            for (int d = 0; d < 4; d++) {
                if (used[d] > capacity[d] + 1e-6) {  // Small epsilon for floating point
                    return "Slot " + s + " exceeds " + dims[d] + " capacity: " +
                            String.format("%.2f > %.2f", used[d], capacity[d]);
                }
            }
        }

        return null;
    }


     // Check F3: SLA windows respected

    private static String checkSLA(Instance instance, Assignment assignment) {
        for (Task task : instance.getTasks()) {
            int slot = assignment.getSlot(task.getId());

            if (task.isSlaViolated(slot)) {
                return "Task " + task.getId() + " assigned to slot " + slot +
                        " but SLA window is [" + task.getSlaStart() + "," + task.getSlaEnd() + "]";
            }
        }

        return null;
    }

    public static class ValidationResult {
        private boolean valid;
        private String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, "Assignment is feasible");
        }

        public static ValidationResult invalid(String reason) {
            return new ValidationResult(false, reason);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return (valid ? "VALID" : "INVALID") + ": " + message;
        }
    }
}
