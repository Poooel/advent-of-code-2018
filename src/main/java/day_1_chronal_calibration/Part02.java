package day_1_chronal_calibration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Part02
{
    public static void main(String[] args) throws Exception
    {
        // Get all the line in the file, as a list of string split on new line separator
        List<String> input = Files.readAllLines(Paths.get("input/day_01.input"));
        // A map of frequencies to check if one already exists
        Map<Integer, Integer> frequencies = new HashMap<>();
        // The frequency we will compute every iteration
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
