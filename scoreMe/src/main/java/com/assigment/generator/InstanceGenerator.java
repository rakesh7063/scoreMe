package com.assigment.generator;

import com.assigment.model.Instance;
import com.assigment.model.Task;
import com.assigment.model.Slot;

import java.util.ArrayList;
import java.util.List;
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
    public static List<BenchmarkConfig> getBenchmarkSuite() {
        List<BenchmarkConfig> benchmarks = new ArrayList<>();

        // Small instances (for brute-force comparison)
        benchmarks.add(new BenchmarkConfig(8, 3, 0.3, 1, "small_8_3"));
        benchmarks.add(new BenchmarkConfig(10, 4, 0.4, 2, "small_10_4"));
        benchmarks.add(new BenchmarkConfig(12, 4, 0.5, 3, "small_12_4"));

        // Medium instances
        benchmarks.add(new BenchmarkConfig(50, 8, 0.25, 10, "medium_50_8"));
        benchmarks.add(new BenchmarkConfig(100, 10, 0.30, 11, "medium_100_10"));
        benchmarks.add(new BenchmarkConfig(150, 12, 0.35, 12, "medium_150_12"));

        // Stress instances
        benchmarks.add(new BenchmarkConfig(200, 15, 0.40, 20, "stress_200_15"));
        benchmarks.add(new BenchmarkConfig(200, 5, 0.60, 21, "stress_200_5_tight"));
        benchmarks.add(new BenchmarkConfig(200, 20, 0.10, 22, "stress_200_20_sparse"));

        return benchmarks;
    }

     // Configuration for a benchmark run

    public static class BenchmarkConfig {
        public int n;
        public int K;
        public double conflictDensity;
        public long seed;
        public String name;

        public BenchmarkConfig(int n, int K, double conflictDensity, long seed, String name) {
            this.n = n;
            this.K = K;
            this.conflictDensity = conflictDensity;
            this.seed = seed;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + " (n=" + n + ", K=" + K + ", density=" + conflictDensity + ")";
        }
    }

}
