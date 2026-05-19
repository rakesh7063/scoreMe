package com.assigment;

import com.assigment.algorithm.ConflictAwareScheduler;
import com.assigment.generator.InstanceGenerator;
import com.assigment.io.JsonIO;
import com.assigment.model.Assignment;
import com.assigment.model.Instance;
import com.assigment.model.Result;
import com.assigment.validator.AssignmentValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class App {
    public static void main(String[] args) throws Exception {
        String command = args.length > 0 ? args[0] : "help";
        System.out.println("Test --- comamnd --> "+ command);
        switch (command) {
            case "generate":
                handleGenerate(args);
                break;
            case "solve":
                handleSolve(args);
                break;
            case "validate":
                handleValidate(args);
                break;
            case "benchmark":
                handleBenchmark(args);
                break;
            case "help":
            default:
                printHelp();
        }

    }
    private static void handleGenerate(String[] args) throws IOException {
        if (args.length < 5) {
            System.out.println("Usage: Please pass more 5 arguments...");
            return;
        }

        int n = Integer.parseInt(args[1]);
        int K = Integer.parseInt(args[2]);
        double density = Double.parseDouble(args[3]);
        long seed = Long.parseLong(args[4]);
        String output = args.length > 5 ? args[5] : "instance_" + n + "_" + K + ".json";

        System.out.println("Generating instance: n=" + n + ", K=" + K + ", density=" + density + ", seed=" + seed);

        Instance instance = InstanceGenerator.generateInstance(n, K, density, seed);
        JsonIO.saveInstance(instance, output);

        System.out.println(" Instance saved to: " + output);
        System.out.println("  Instance: " + instance);
    }
    private static void handleSolve(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("args test -->"+ args.length);
            System.out.println("Usage: add valid json file path..");
            return;
        }

        String inputFile = args[1];
        String outputFile = args.length > 2 ? args[2] : "result_" + System.currentTimeMillis() + ".json";

        System.out.println("Solving instance from: " + inputFile);

        Instance instance = JsonIO.loadInstance(inputFile);
        System.out.println("  Loaded: " + instance);

        // Run scheduler
        ConflictAwareScheduler scheduler = new ConflictAwareScheduler(instance);
        Result result = scheduler.schedule();

        // Validate if feasible
        if (result.isFeasible()) {
            AssignmentValidator.ValidationResult validation =
                    AssignmentValidator.validate(instance, result.getAssignment());
            System.out.println("  Validation: " + validation);
        }

        JsonIO.saveResult(result, outputFile);

        System.out.println("  Runtime: " + result.getRuntimeMs() + " ms");
        System.out.println("  Feasible: " + result.isFeasible());
        System.out.println("  Penalty: " + result.getPenalty());
        System.out.println(" Result saved to: " + outputFile);
    }
    private static void handleValidate(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: please add instance file and result file...");
            return;
        }

        String instanceFile = args[1];
        String resultFile = args[2];

        System.out.println("Validating result...");

        Instance instance = JsonIO.loadInstance(instanceFile);
        System.out.println("  Instance: " + instance);

        Result result = JsonIO.loadResult(resultFile);
        System.out.println("  Result metadata: feasible=" + result.isFeasible() + ", penalty=" + result.getPenalty() + ", runtime_ms=" + result.getRuntimeMs());
        if (!result.isFeasible()) {
            System.out.println("  Result marked infeasible. Violation: " + result.getViolationReason());
            return;
        }

        Assignment assignment = JsonIO.loadAssignment(instance, resultFile);
        if (assignment == null) {
            System.out.println("  No assignment data found in result file.");
            return;
        }

        AssignmentValidator.ValidationResult validation = AssignmentValidator.validate(instance, assignment);
        System.out.println("  Validation: " + validation);
        if (!validation.isValid()) {
            System.out.println("  Warning: result is not actually feasible for the provided instance.");
        }
    }
    private static void handleBenchmark(String[] args) throws IOException {
        System.out.println("Running full benchmark suite...\n");

        List<InstanceGenerator.BenchmarkConfig> benchmarks = InstanceGenerator.getBenchmarkSuite();
        List<BenchmarkResult> results = new ArrayList<>();

        System.out.println(String.format("%-30s | %5s | %5s | %8s | %6s | %10s",
                "Instance", "n", "K", "Penalty", "Time(ms)", "Feasible"));
        System.out.println("-".repeat(80));

        int completed = 0;
        for (InstanceGenerator.BenchmarkConfig config : benchmarks) {
            try {
                Instance instance = InstanceGenerator.generateInstance(config.n, config.K, config.conflictDensity, config.seed);

                long startTime = System.currentTimeMillis();
                ConflictAwareScheduler scheduler = new ConflictAwareScheduler(instance);
                Result result = scheduler.schedule();
                long elapsedTime = System.currentTimeMillis() - startTime;

                BenchmarkResult br = new BenchmarkResult(config.name, config.n, config.K, result);
                results.add(br);

                System.out.println(String.format("%-30s | %5d | %5d | %8.2f | %6d | %10s",
                        config.name,
                        config.n,
                        config.K,
                        result.getPenalty(),
                        result.getRuntimeMs(),
                        result.isFeasible() ? "YES" : "NO"));

                completed++;
            } catch (Exception e) {
                System.err.println("Error on " + config.name + ": " + e.getMessage());
            }
        }

        System.out.println("-".repeat(80));
        System.out.println("Completed: " + completed + "/" + benchmarks.size());

        // Save summary
        saveBenchmarkSummary(results);
    }

    private static void saveBenchmarkSummary(List<BenchmarkResult> results) throws IOException {
        Path reportPath = Paths.get("benchmark_report_" + System.currentTimeMillis() + ".txt");

        StringBuilder sb = new StringBuilder();
        sb.append("MSME CREDIT PIPELINE SCHEDULER - BENCHMARK REPORT\n");
        sb.append("=".repeat(80)).append("\n\n");

        sb.append(String.format("%-30s | %5s | %5s | %8s | %6s | %10s\n",
                "Instance", "n", "K", "Penalty", "Time(ms)", "Feasible"));
        sb.append("-".repeat(80)).append("\n");

        int feasibleCount = 0;
        double totalPenalty = 0;
        long totalTime = 0;

        for (BenchmarkResult br : results) {
            sb.append(String.format("%-30s | %5d | %5d | %8.2f | %6d | %10s\n",
                    br.name, br.n, br.K, br.penalty, br.runtimeMs, br.feasible ? "YES" : "NO"));

            if (br.feasible) {
                feasibleCount++;
                totalPenalty += br.penalty;
                totalTime += br.runtimeMs;
            }
        }

        sb.append("-".repeat(80)).append("\n");
        sb.append("SUMMARY\n");
        sb.append("  Feasible instances: ").append(feasibleCount).append("/").append(results.size()).append("\n");
        sb.append("  Average penalty (feasible): ").append(String.format("%.2f", totalPenalty / Math.max(1, feasibleCount))).append("\n");
        sb.append("  Average runtime (feasible): ").append(String.format("%.2f", totalTime / Math.max(1.0, feasibleCount))).append(" ms\n");

        Files.writeString(reportPath, sb.toString());
        System.out.println("\n Benchmark report saved: " + reportPath);
    }

    private static void printHelp() {
        System.out.println("MSME Credit Pipeline Scheduler\n");
        System.out.println("Commands:");
        System.out.println("  generate <n> <K> <density> <seed> [output]");
        System.out.println("    Generate a random instance\n");
        System.out.println("  solve <instance_file> [output_file]");
        System.out.println("    Solve an instance and output result\n");
        System.out.println("  validate <instance_file> <result_file>");
        System.out.println("    Validate a result against constraints\n");
        System.out.println("  benchmark");
        System.out.println("    Run full benchmark suite\n");
    }
    static class BenchmarkResult {
        String name;
        int n;
        int K;
        double penalty;
        long runtimeMs;
        boolean feasible;

        BenchmarkResult(String name, int n, int K, Result result) {
            this.name = name;
            this.n = n;
            this.K = K;
            this.penalty = result.getPenalty();
            this.runtimeMs = result.getRuntimeMs();
            this.feasible = result.isFeasible();
        }
    }
}
