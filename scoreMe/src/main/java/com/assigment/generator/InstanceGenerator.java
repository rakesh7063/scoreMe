package com.assigment.generator;

import com.assigment.model.Instance;
import com.assigment.model.Task;
import com.assigment.model.Slot;

import java.util.Random;

public class InstanceGenerator {
    public static Instance generateInstance(int n, int K, double conflictDensity, long seed) {
        Random random = new Random(seed);
        Instance instance = new Instance(n, K);

        // Create tasks
        for (int i = 0; i < n; i++) {
            String taskId = "T" + i;

            // Resource requirements [CPU, RAM, GPU, Network]
            double[] resources = new double[4];
            int[] baseCap = {32, 128, 8, 6};  // Base capacities
            for (int d = 0; d < 4; d++) {
                resources[d] = random.nextDouble() * (baseCap[d] / (n / K + 1));
            }

            // Priority weight
            double weight = 1 + random.nextDouble() * 9;

            // SLA window [lo, hi]
            int lo = random.nextInt(Math.max(1, K - 1));
            int hi = lo + 1 + random.nextInt(K - lo);
            hi = Math.min(hi, K - 1);

            Task task = new Task(taskId, resources, weight, lo, hi);
            instance.addTask(task);
        }

        // Add conflicts
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (random.nextDouble() < conflictDensity) {
                    instance.addConflict(i, j);
                }
            }
        }

        // Create slots with capacities
        double[] baseCapacity = {32, 128, 8, 6.0};
        for (int k = 0; k < K; k++) {
            double[] slotCapacity = baseCapacity.clone();
            Slot slot = new Slot(k, slotCapacity);
            instance.addSlot(slot);
        }

        return instance;
    }

}
