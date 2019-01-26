package days;

import com.google.common.base.Functions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Day12_SubterraneanSustainability implements Executable {
    @Override
    public String executePartOne() {
        // Parse the input file
        List<String> inputs = ChallengeHelper.readInputData(12);
        // Parse the input to get the rules and initial state
        ParseContext parseContext = parseInput(inputs);

        // Initialize the currentGeneration to initialState
        Set<Integer> currentGeneration = parseContext.getInitialState();

        // Compute the 20 generations asked
        for (int i = 0; i < 20; i++) {
            currentGeneration = computeNextGeneration(currentGeneration, parseContext.getRules());
        }

        // Return the number of plants for the final generation
        return String.valueOf(sumOfSet(currentGeneration));
    }

    @Override
    public String executePartTwo() {
        // Parse the input file
        List<String> inputs = ChallengeHelper.readInputData(12);
        // Parse the input to get the rules and initial state
        ParseContext parseContext = parseInput(inputs);
        GenerationContext generationContext = computeNextGenerationSpecial(parseContext);
        // Return the sum of the 500 generations we computed +
        // 50_000_000_000 (which is the number of generations asked) - the number of generations
        // we computed (500) and times the most common value we found in the first 500 generations
        return String.valueOf(
            generationContext.getSumOfPlants() +
                ((50_000_000_000L - generationContext.getGenerationComputed())
                    * generationContext.getMostRecurrentValue())
        );
    }

    private int sumOfSet(Set<Integer> set) {
        return set
            // To make the sum of a set, we stream it
            .stream()
            // Map it to an IntStream
            .mapToInt(Integer::intValue)
            // Then use the sum method
            .sum();
    }

    private ParseContext parseInput(List<String> inputs) {
        // Parse the initial state, we know it is after the colon
        String initialState = inputs.get(0).split(":")[1].trim();
        Map<String, String> rules = new HashMap<>();

        // Starting at line 3 because line 1 is for initial state and line 2 is blank
        for (int i = 2; i < inputs.size(); i++) {
            // Split on the arrow, on right it is the pattern, on left it is the result
            String[] splitInput = inputs.get(i).split("=>");
            // We only keep a pattern if the result is a plant
            if (splitInput[1].trim().equals("#")) {
                rules.put(splitInput[0].trim(), splitInput[1].trim());
            }
        }

        return new ParseContext(rules, parseInitialState(initialState));
    }

    private Set<Integer> parseInitialState(String initialState) {
        Set<Integer> initialSet = new HashSet<>();

        // Add all the indices of the pot with a plant in the initial state
        for (int i = 0; i < initialState.length(); i++) {
            if (initialState.charAt(i) == '#') {
                initialSet.add(i);
            }
        }

        return initialSet;
    }

    // Solution from:
    // https://www.reddit.com/r/adventofcode/comments/a5eztl/2018_day_12_solutions/ebm4c9d
    private Set<Integer> computeNextGeneration(Set<Integer> generation, Map<String, String> rules) {
        // Start at the minimum in the list and substracting 3 to have some free space at the beginning
        int start = Collections.min(generation) - 3;
        // Do the same thing at the end but adding 4 instead of 3 because the for loop using strictly less than
        int end = Collections.max(generation) + 4;
        // The set only hold the indices of the pot where there is a plant
        Set<Integer> nextGeneration = new HashSet<>();

        for (int i = start; i < end; i++) {
            // Create a StringBuilder to create the pattern we are doing
            StringBuilder pattern = new StringBuilder();
            for (int j = -2; j < 3; j++) {
                // If the indices is in the generationSet, it means it is a pot with a plant
                // so add a #
                if (generation.contains(i + j)) {
                    pattern.append("#");
                } else {
                    pattern.append(".");
                }
            }

            // If the pattern is in the rules add the indices to the set
            if (rules.containsKey(pattern.toString())) {
                nextGeneration.add(i);
            }
        }

        return nextGeneration;
    }

    private GenerationContext computeNextGenerationSpecial(ParseContext parseContext) {
        // Initialize the currentGeneration as the initial state
        Set<Integer> currentGeneration = parseContext.getInitialState();
        // List to store all the differences between the generations to find a recurring pattern
        List<Integer> differencesWithLastGeneration = new ArrayList<>();

        // Compute only the first 500 generations as it should be enough to
        // find a recurring pattern
        for (int i = 0; i < 500; i++) {
            // Compute the next generation
            Set<Integer> nextGeneration = computeNextGeneration(currentGeneration, parseContext.getRules());
            // Store the differences between nextGeneration and the currentGeneration
            differencesWithLastGeneration.add(sumOfSet(nextGeneration) - sumOfSet(currentGeneration));
            // Assign the nextGeneration to currentGeneration and start again
            currentGeneration = nextGeneration;
        }

        return new GenerationContext(
            sumOfSet(currentGeneration),
            mostCommonValue(differencesWithLastGeneration),
            500
        );
    }

    // https://stackoverflow.com/a/36105483/7621349
    private int mostCommonValue(List<Integer> numbers) {
        return numbers
            // Stream the list of numbers
            .stream()
            // Collect the numbes into a map with the key being the number and the value
            // the number of times they appear in the list
            .collect(Collectors.groupingBy(Functions.identity(), Collectors.counting()))
            // Get the entrySet
            .entrySet()
            // Stream it
            .stream()
            // Find the max comparing using the value
            .max(Comparator.comparing(Map.Entry::getValue))
            // Get the optional
            .get()
            // Get the key as this the number most common in the given list
            .getKey();
    }

    @Value
    private final class ParseContext {
        private Map<String, String> rules;
        private Set<Integer> initialState;
    }

    @Value
    private final class GenerationContext {
        private int sumOfPlants;
        private int mostRecurrentValue;
        private int generationComputed;
    }
}
