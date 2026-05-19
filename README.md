# ScoreMe Scheduler

A Java-based heuristic scheduling system developed for the ScoreMe Engineering Capstone Assignment.

This project solves a constrained multi-resource scheduling problem involving:

- Conflict-aware task scheduling
- SLA-constrained execution windows
- Multi-dimensional resource allocation
- Feasibility validation
- Penalty minimization
- Benchmark-driven evaluation

---

# Tech Stack

- Java 21
- Maven
- Jackson Databind
- JUnit 5

---

# Project Structure

```text
scoreMe
│
├── pom.xml
├── src
│   ├── main
│   │   └── java/com/assigment
│   │       │
│   │       ├── App.java
│   │       │
│   │       ├── algorithm
│   │       │   └── ConflictAwareScheduler.java
│   │       │
│   │       ├── generator
│   │       │   └── InstanceGenerator.java
│   │       │
│   │       ├── io
│   │       │   └── JsonIO.java
│   │       │
│   │       ├── model
│   │       │   ├── Assignment.java
│   │       │   ├── ConflictGraph.java
│   │       │   ├── Instance.java
│   │       │   ├── Result.java
│   │       │   ├── Slot.java
│   │       │   └── Task.java
│   │       │
│   │       └── validator
│   │           └── AssignmentValidator.java
│   │
│   └── test
│       └── java/com/assigment
│           │
│           ├── generator
│           │   └── InstanceGeneratorTest.java
│           │
│           ├── io
│           │   └── JsonIOTest.java
│           │
│           ├── model
│           │   ├── ConflictGraphTest.java
│           │   └── ModelTests.java
│           │
│           └── validator
│               └── AssignmentValidatorTest.java
```

# Problem Statement

The scheduler assigns MSME credit pipeline tasks into execution slots while satisfying:

- Conflict constraints
- Resource capacity constraints
- SLA window constraints

The objective is to minimize overall weighted scheduling penalty while maintaining feasibility.

---
# Features
- Conflict-aware scheduling
- Resource capacity validation
- SLA window enforcement
- Benchmark instance generation
- Assignment feasibility checking
- JSON input/output support
- Runtime and penalty tracking
- Benchmark report generation
- Unit testing support
---
# Scheduling Strategy

The scheduler uses a greedy heuristic scheduling approach:

1. Analyze task conflicts
2. Evaluate resource constraints
3. Respect SLA windows
4. Assign tasks to feasible execution slots
5. Minimize overall scheduling penalty
---
# Constraints Handled
## Conflict Constraints

Conflicting tasks cannot execute in the same slot.

Examples:

- Shared GPU contention
- Kafka partition conflict
---
# Resource Constraints

Each slot has limited:

- CPU
- RAM
- GPU
- Network bandwidth
---
# SLA Constraints

Tasks must execute only within their allowed scheduling window.

---
# Build Project
```Bash
 mvn clean install
 ```
---
# Run Commands
## Generate Benchmark Instance
```` Bash
java -cp target/classes com.assigment.App generate 50 8 0.3 42
````
## Solve Scheduling Instance
````Bash
java -cp target/classes com.assigment.App solve instance_50_8.json
````
## Validate Assignment
````Bash
java -cp target/classes com.assigment.App validate instance_50_8.json result.json
````
## Run Benchmark Suite
````Bash
java -cp target/classes com.assigment.App benchmark
````
---
# Example Output
````Json
{
  "assignment": {
    "T1": 1,
    "T2": 2,
    "T3": 2
  },
  "penalty": 15.0,
  "runtime_ms": 12,
  "feasible": true,
  "violation": ""
}
````
---
# Testing

Run unit tests using:
````bash
mvn test
````

Current test coverage includes:

- Instance generation
- JSON parsing
- Conflict graph validation
- Assignment validation
- Model testing
- Benchmark Goals
---
