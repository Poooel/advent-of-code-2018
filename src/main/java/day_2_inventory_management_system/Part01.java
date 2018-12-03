package day_2_inventory_management_system;

import com.google.common.base.CharMatcher;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Part01
{
    public static void main(String [ ] args) throws Exception
    {
        // Get all the line in the file, as a list of string split on new line separator
        List<String> inputs = Files.readAllLines(Paths.get("data/day_02.input"));
        // Keep the counter of the occurences of two chars and three chars
        int twos = 0;
        int threes = 0;

        // For each string in the input
        for (String input : inputs) {
            // Make a list of excluded chars to not count the one
            // we already processed
            List<Character> excluded = new ArrayList<>();
            // Use a flag as if there is two times two chars, it only counts as one e.g
            // aabbcd, there is 'aa' and 'bb' but it counts only as one
            boolean twosFlag = false;
            // Same for three occurence
            boolean threesFlag = false;

            // For each char in the string
            for (char c : input.toCharArray()) {
                // If the char is excluded don't bother processing and continue
                if (excluded.contains(c)) {
                    continue;
                }
                // Count the number of occurences of the char in the string
                int count = CharMatcher.is(c).countIn(input);
                // Add the char to the excluded list
                excluded.add(c);
                // If the count is 2, set the two flag to true
                if (count == 2) {
                    twosFlag = true;
                } else if (count == 3) {
                    // If the count is 3, set the three flag to true
                    threesFlag = true;
                }
            }

            // If the two flag is set to true, increment by one the twos counter
            if (twosFlag) {
                twos++;
            }

            // Same here for the three flag and threes counter
            if (threesFlag) {
                threes++;
            }
        }

        // Print the solution as twos * threes
        System.out.println(String.format("The answer is %s", twos * threes));
    }
}
