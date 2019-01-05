package days;

import launcher.ChallengeHelper;
import launcher.Executable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChronalCalibration implements Executable {
    @Override
    public String executePartOne() {
        return String.valueOf(
            ChallengeHelper.readInputData(1)
            .stream()
            // Transform the stream into an IntStream by parsing the string
            .mapToInt(Integer::parseInt)
            // Make the sum of it
            .sum()
        );
    }

    @Override
    public String executePartTwo() {
        List<String> input = ChallengeHelper.readInputData(1);
        // A map of frequencies to check if one already exists
        // we use a map because it is faster for lookups
        Map<Integer, Integer> frequencies = new HashMap<>();
        // The frequency we will compute at every iteration
        int frequency = 0;

        // Convert the list of strings to a list of ints so we won't have to parse it
        // at every iteration
        // https://stackoverflow.com/a/23674719/7621349
        List<Integer> parsedInput = input
            .stream()
            .mapToInt(Integer::parseInt)
            .boxed()
            .collect(Collectors.toList());

        // Using an infinite loop to loop over the list while a result hasn't been found
        while (true) {
            for (int number : parsedInput) {
                frequency += number;

                // Check if we already encountered this frequency
                if (frequencies.containsKey(frequency)) {
                    // If the frequency is present return it because this is the one
                    // we are looking for
                    return String.valueOf(frequency);
                }

                // If the frequency was not present, add it
                frequencies.put(frequency, frequency);
            }
        }
    }
}
