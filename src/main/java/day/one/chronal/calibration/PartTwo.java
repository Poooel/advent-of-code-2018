package day.one.chronal.calibration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartTwo
{
    public static void main(String [ ] args) throws Exception
    {
        List<String> input = Files.readAllLines(Paths.get("data/day-one.data"));
        Map<Integer, Integer> frequencies = new HashMap<>();
        int frequency = 0;

        // While true loop to loop over the list while a result hasn't been found
        while (true) {
            // Iterate over the list of input
            for (String number : input) {
                // Parse the input and add it to the frequency
                frequency += Integer.parseInt(number);

                // Check if we already encountered this frequency using a hashmap for speed
                if (frequencies.containsKey(frequency)) {
                    // If the frequency is present,
                    // halt the execution by throwing an error with the result
                    throw new Exception(String.format("The answer is %s", frequency));
                }

                // If the frequency was not present, add it
                frequencies.put(frequency, frequency);
            }
        }
    }
}
