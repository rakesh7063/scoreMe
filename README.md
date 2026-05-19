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
│
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
