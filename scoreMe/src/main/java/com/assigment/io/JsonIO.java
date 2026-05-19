package com.assigment.io;

import com.assigment.model.*;
import com.google.gson.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonIO {
    public static void saveInstance(Instance instance, String filePath) throws IOException {
        JsonObject json = new JsonObject();

        // Tasks
        JsonArray tasksJson = new JsonArray();
        for (Task task : instance.getTasks()) {
            JsonObject taskJson = new JsonObject();
            taskJson.addProperty("id", task.getId());
            taskJson.addProperty("weight", task.getWeight());

            JsonArray resourcesJson = new JsonArray();
            for (double r : task.getResources()) {
                resourcesJson.add(r);
            }
            taskJson.add("resources", resourcesJson);

            taskJson.addProperty("sla_start", task.getSlaStart());
            taskJson.addProperty("sla_end", task.getSlaEnd());
            tasksJson.add(taskJson);
        }
        json.add("tasks", tasksJson);

        // Conflicts
        JsonArray conflictsJson = new JsonArray();
        for (ConflictGraph.Pair<Integer, Integer> edge : instance.getConflictGraph().getEdges()) {
            JsonArray conflictJson = new JsonArray();
            conflictJson.add(edge.first);
            conflictJson.add(edge.second);
            conflictsJson.add(conflictJson);
        }
        json.add("conflicts", conflictsJson);

        // Capacities
        JsonArray capacitiesJson = new JsonArray();
        for (Slot slot : instance.getSlots()) {
            JsonArray capJson = new JsonArray();
            for (double c : slot.getCapacity()) {
                capJson.add(c);
            }
            capacitiesJson.add(capJson);
        }
        json.add("capacities", capacitiesJson);

        json.addProperty("K", instance.getK());
        json.addProperty("n", instance.getN());

        // Write to file
        try (FileWriter writer = new FileWriter(filePath)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
        }
    }
    public static Instance loadInstance(String filePath) throws IOException {
        JsonObject json;
        try (FileReader reader = new FileReader(filePath)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        }

        int K = json.get("K").getAsInt();
        int n = json.get("n").getAsInt();
        Instance instance = new Instance(n, K);

        // Load tasks
        JsonArray tasksJson = json.getAsJsonArray("tasks");
        for (JsonElement taskElem : tasksJson) {
            JsonObject taskJson = taskElem.getAsJsonObject();
            String id = taskJson.get("id").getAsString();
            double weight = taskJson.get("weight").getAsDouble();
            int slaStart = taskJson.get("sla_start").getAsInt();
            int slaEnd = taskJson.get("sla_end").getAsInt();

            JsonArray resourcesJson = taskJson.getAsJsonArray("resources");
            double[] resources = new double[4];
            for (int i = 0; i < 4; i++) {
                resources[i] = resourcesJson.get(i).getAsDouble();
            }

            Task task = new Task(id, resources, weight, slaStart, slaEnd);
            instance.addTask(task);
        }

        // Load conflicts
        JsonArray conflictsJson = json.getAsJsonArray("conflicts");
        for (JsonElement conflictElem : conflictsJson) {
            JsonArray conflictJson = conflictElem.getAsJsonArray();
            int i = conflictJson.get(0).getAsInt();
            int j = conflictJson.get(1).getAsInt();
            instance.addConflict(i, j);
        }

        // Load capacities
        JsonArray capacitiesJson = json.getAsJsonArray("capacities");
        for (JsonElement capElem : capacitiesJson) {
            JsonArray capJson = capElem.getAsJsonArray();
            double[] capacity = new double[4];
            for (int i = 0; i < 4; i++) {
                capacity[i] = capJson.get(i).getAsDouble();
            }
            instance.addSlot(new Slot(instance.getSlots().size(), capacity));
        }

        return instance;
    }
    public static void saveResult(Result result, String filePath) throws IOException {
        JsonObject json = new JsonObject();

        json.addProperty("feasible", result.isFeasible());
        json.addProperty("penalty", result.getPenalty());
        json.addProperty("runtime_ms", result.getRuntimeMs());

        if (result.isFeasible()) {
            JsonObject assignmentJson = new JsonObject();
            for (Map.Entry<String, Integer> entry : result.getAssignment().getAssignment().entrySet()) {
                assignmentJson.addProperty(entry.getKey(), entry.getValue());
            }
            json.add("assignment", assignmentJson);
        } else {
            json.addProperty("violation_reason", result.getViolationReason());
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
        }
    }

     // Load result metadata from JSON file. Does not attempt to reconstruct the assignment without instance context.
    public static Result loadResult(String filePath) throws IOException {
        JsonObject json;
        try (FileReader reader = new FileReader(filePath)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        }

        boolean feasible = json.get("feasible").getAsBoolean();
        double penalty = json.get("penalty").getAsDouble();
        long runtimeMs = json.get("runtime_ms").getAsLong();
        String violation = feasible ? null : json.get("violation_reason").getAsString();

        return new Result(null, penalty, runtimeMs, feasible, violation);
    }

     // Load assignment from JSON file using instance context.
    public static Assignment loadAssignment(Instance instance, String filePath) throws IOException {
        JsonObject json;
        try (FileReader reader = new FileReader(filePath)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        }

        if (!json.get("feasible").getAsBoolean()) {
            return null;
        }

        if (!json.has("assignment")) {
            return null;
        }

        JsonObject assignmentJson = json.getAsJsonObject("assignment");
        Map<String, Integer> assignmentMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : assignmentJson.entrySet()) {
            assignmentMap.put(entry.getKey(), entry.getValue().getAsInt());
        }

        return Assignment.fromMap(instance, assignmentMap);
    }


}
