package day_4_repose_record;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Part02 {
    public static void main(String[] args) throws Exception {
        // Get all the line in the file, as a list of string split on new line separator
        List<String> inputs = Files.readAllLines(Paths.get("input/day_04.input"));

        // We can just sort the inputs, with a default sorting because the date format
        // is sortable
        Collections.sort(inputs);

        // Parse the input to get the guards
        Map<Integer, Guard> guards = Part01.parseInput(inputs);

        // Get a list of context for each guard
        List<MaximumContext> maximumContexts = new ArrayList<>();

        for (Guard guard : guards.values()) {
            // Compute the most aslept minute for each guard
            maximumContexts.add(Part01.findMostAsleepMinute(guard));
        }

        // Get the context with the most time asleep on the minute
        MaximumContext maximumContext = maximumContexts
            // Stream the list of context
            .stream()
            // Use max operation using the getMaximum of the MaximumContext object to compare
            .max(Comparator.comparingInt(MaximumContext::getMaximum))
            // Get because max return an optional
            .get();

        // Print the answer
        System.out.println(String.format("The answer is: %s", maximumContext.getGuardId() * maximumContext.getIndex()));
    }
}
