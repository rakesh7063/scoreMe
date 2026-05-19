package com.assigment.io;

import com.assigment.model.ConflictGraph;
import com.assigment.model.Instance;
import com.assigment.model.Slot;
import com.assigment.model.Task;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;

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

}
