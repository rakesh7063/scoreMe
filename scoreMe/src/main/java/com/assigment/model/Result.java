package com.assigment.model;

public class Result {
    private Assignment assignment;
    private double penalty;
    private long runtimeMs;
    private boolean feasible;
    private String violationReason;

    public Result(Assignment assignment, double penalty, long runtimeMs, boolean feasible, String violationReason) {
        this.assignment = assignment;
        this.penalty = penalty;
        this.runtimeMs = runtimeMs;
        this.feasible = feasible;
        this.violationReason = violationReason;
    }

    public static Result infeasible(String reason, long runtimeMs) {
        return new Result(null, Double.MAX_VALUE, runtimeMs, false, reason);
    }

    public static Result feasible(Assignment assignment, double penalty, long runtimeMs) {
        return new Result(assignment, penalty, runtimeMs, true, null);
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public double getPenalty() {
        return penalty;
    }

    public long getRuntimeMs() {
        return runtimeMs;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public String getViolationReason() {
        return violationReason;
    }

    @Override
    public String toString() {
        return "Result{" +
                "feasible=" + feasible +
                ", penalty=" + String.format("%.2f", penalty) +
                ", runtimeMs=" + runtimeMs +
                (violationReason != null ? ", violation='" + violationReason + '\'' : "") +
                '}';
    }
}
