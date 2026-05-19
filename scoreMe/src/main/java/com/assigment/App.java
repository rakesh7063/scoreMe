package com.assigment;

import com.assigment.algorithm.ConflictAwareScheduler;
import com.assigment.generator.InstanceGenerator;
import com.assigment.io.JsonIO;
import com.assigment.model.Instance;
import com.assigment.model.Result;
import com.assigment.validator.AssignmentValidator;

import java.io.IOException;


public class App {
    public static void main(String[] args) throws Exception {
        String command = args.length > 0 ? args[0] : "help";
        System.out.println("Test --- comamnd --> "+ command);
        switch (command) {
            case "generate":
                handleGenerate(args);
                break;
            case "slove":
            handleSolve(args);
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

}
