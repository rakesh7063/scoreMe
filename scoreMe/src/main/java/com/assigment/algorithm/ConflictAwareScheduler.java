package com.assigment.algorithm;


import com.assigment.model.Assignment;
import com.assigment.model.Instance;
import com.assigment.model.Result;
import com.assigment.model.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConflictAwareScheduler {
    private Instance instance;
    private Assignment assignment;
    private double[][] slotUsage;  // [slot][dimension] -> used resource amount
    private boolean[] taskAssigned;
    private boolean feasible;
    private String infeasibilityReason;

    public ConflictAwareScheduler(Instance instance) {
        this.instance = instance;
        this.assignment = new Assignment(instance);
        this.slotUsage = new double[instance.getK()][4];

        for (int s = 0; s < instance.getK(); s++) {
            Arrays.fill(slotUsage[s], 0.0);
        }
        this.taskAssigned = new boolean[instance.getN()];
        Arrays.fill(taskAssigned, false);
    }

    // run algorithm schedule
    public Result schedule() {
        long startTime = System.currentTimeMillis();

        try {
            // Try greedy assignment with DSATUR ordering
            List<Integer> taskOrder = getTaskOrderByConflictSaturation();

            for (int taskIdx : taskOrder) {
                Task task = instance.getTask(taskIdx);


                int selectedSlot = findBestSlot(taskIdx);

                if (selectedSlot == -1) {

                    selectedSlot = findAnyValidSlot(taskIdx);
                }

                if (selectedSlot == -1) {
                    feasible = false;
                    infeasibilityReason = "No valid slot found for task " + task.getId() +
                            ". Likely due to: insufficient capacity, SLA constraints, or conflict saturation.";
                    long endTime = System.currentTimeMillis();
                    return Result.infeasible(infeasibilityReason, endTime - startTime);
                }

                // Assign task to slot
                assignment.assign(task.getId(), selectedSlot);
                taskAssigned[taskIdx] = true;
                updateSlotLoad(selectedSlot, taskIdx);
            }

            // Calculate penalty
            double penalty = calculateExtendedPenalty();

            feasible = true;
            long endTime = System.currentTimeMillis();
            return Result.feasible(assignment, penalty, endTime - startTime);

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            return Result.infeasible("Exception: " + e.getMessage(), endTime - startTime);
        }
    }

// Fallback: Find any valid slot without multi-criteria ranking
    private int findAnyValidSlot(int taskIdx) {
        for (int s = 0; s < instance.getK(); s++) {
            if (isSlotFeasible(taskIdx, s)) {
                return s;
            }
        }
        return -1;
    }
    private List<Integer> getTaskOrderByConflictSaturation() {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < instance.getN(); i++) {
            order.add(i);
        }

        // Sort by conflict count (descending) - greedy DSATUR heuristic
        order.sort((i, j) -> {
            int conflictI = instance.getConflictGraph().getConflictCount(i);
            int conflictJ = instance.getConflictGraph().getConflictCount(j);
            return Integer.compare(conflictJ, conflictI);  // Descending order
        });

        return order;
    }
    private int selectBestSlot(int taskIdx, List<Integer> validSlots) {
        Task task = instance.getTask(taskIdx);

        return validSlots.stream()
                .min((s1, s2) -> {
                    // Criterion 1: Load balance (lower total utilization is better)
                    double load1 = calculateSlotLoadWithTask(s1, taskIdx);
                    double load2 = calculateSlotLoadWithTask(s2, taskIdx);
                    int loadCompare = Double.compare(load1, load2);
                    if (loadCompare != 0) return loadCompare;

                    // Criterion 2: Minimize delay (prefer earlier slots)
                    int delayCompare = Integer.compare(s1, s2);
                    if (delayCompare != 0) return delayCompare;

                    // Criterion 3: SLA slack (prefer slots with larger margin to SLA end)
                    int slack1 = task.getSlaEnd() - s1;
                    int slack2 = task.getSlaEnd() - s2;
                    return Integer.compare(slack2, slack1);
                })
                .orElse(-1);
    }

//Find the best slot for a task using multi-criteria evaluation
    private int findBestSlot(int taskIdx) {
        Task task = instance.getTask(taskIdx);
        List<Integer> validSlots = new ArrayList<>();


        for (int s = 0; s < instance.getK(); s++) {
            if (isSlotFeasible(taskIdx, s)) {
                validSlots.add(s);
            }
        }

        if (validSlots.isEmpty()) {
            return -1;
        }


        return selectBestSlot(taskIdx, validSlots);
    }


    // Check if a slot is feasible for a task (no conflicts, capacity, SLA)
    private boolean isSlotFeasible(int taskIdx, int slotIdx) {
        Task task = instance.getTask(taskIdx);

        // Check SLA constraint
        if (task.isSlaViolated(slotIdx)) {
            return false;
        }

        // Check resource capacity
        double[] available = getAvailableCapacity(slotIdx);
        for (int d = 0; d < 4; d++) {
            if (task.getResources()[d] > available[d]) {
                return false;
            }
        }

        // Check conflict constraint
        for (String conflictTaskId : instance.getConflictGraph().getConflicts(taskIdx)) {
            int conflictIdx = Integer.parseInt(conflictTaskId.substring(1));
            if (taskAssigned[conflictIdx]) {
                int conflictSlot = assignment.getSlot(conflictTaskId);
                if (conflictSlot == slotIdx) {
                    return false;
                }
            }
        }

        return true;
    }

    //Calculate what the slot load would be if we assign this task (normalized)
    private double calculateSlotLoadWithTask(int slotIdx, int taskIdx) {
        Task task = instance.getTask(taskIdx);
        double[] capacity = instance.getSlot(slotIdx).getCapacity();

        // Normalized load (0-1 range) - average across all dimensions
        double totalLoad = 0;
        for (int d = 0; d < 4; d++) {
            totalLoad += (slotUsage[slotIdx][d] + task.getResources()[d]) / capacity[d];
        }
        return totalLoad / 4;
    }


     //Update slot load tracking when task is assigned
    private void updateSlotLoad(int slotIdx, int taskIdx) {
        Task task = instance.getTask(taskIdx);
        double[] resources = task.getResources();

        for (int d = 0; d < 4; d++) {
            slotUsage[slotIdx][d] += resources[d];
        }
    }

// Get available capacity in a slot
    private double[] getAvailableCapacity(int slotIdx) {
        double[] capacity = instance.getSlot(slotIdx).getCapacity().clone();

        // Available = capacity - used
        for (int d = 0; d < 4; d++) {
            capacity[d] -= slotUsage[slotIdx][d];
        }
        return capacity;
    }
    private double calculateExtendedPenalty() {
        double penalty = 0;


        double pBase = 0;
        for (Task task : instance.getTasks()) {
            int slot = assignment.getSlot(task.getId());
            pBase += task.getWeight() * slot;
        }

        double[] slotUtilization = new double[instance.getK()];
        for (int s = 0; s < instance.getK(); s++) {
            double totalUtil = 0;
            double[] capacity = instance.getSlot(s).getCapacity();
            for (int d = 0; d < 4; d++) {
                totalUtil += slotUsage[s][d] / capacity[d];
            }
            slotUtilization[s] = totalUtil / 4;
        }

        double meanUtilization = Arrays.stream(slotUtilization).average().orElse(0);
        double pLoadImbalance = 0;
        for (double util : slotUtilization) {
            pLoadImbalance += Math.pow(util - meanUtilization, 2);
        }
        pLoadImbalance = Math.sqrt(pLoadImbalance);

        // P_sla_slack: Penalize tasks assigned near their SLA end window
        double pSlaSlack = 0;
        for (Task task : instance.getTasks()) {
            int slot = assignment.getSlot(task.getId());
            int slack = task.getSlaEnd() - slot;
            if (slack < 2) {
                pSlaSlack += task.getWeight() * (2 - slack);
            }
        }

        // Combined penalty with weights
        penalty = pBase + 0.5 * pLoadImbalance + 2.0 * pSlaSlack;

        return penalty;
    }

    public boolean isFeasible() {
        return feasible;
    }
}
