package day_5_alchemical_reduction;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Part02 {
    public static void main(String[] args) throws Exception
    {
        // Get the line in the file, as day 5 input is only one line
        List<String> inputs = Files.readAllLines(Paths.get("input/day_05.input"));
        // Get the first line as the input is only one line
        String input = inputs.get(0);

        // Get all the distinct chars
        List<String> letters = input
            // Lower case the input to get only distinct letter
            .toLowerCase()
            // Convert it to a IntStream
            .chars()
            // Get only the distinct values
            .distinct()
            // Convert the IntStream to Stream<Integer>
            .boxed()
            // Convert each integer to a char then to a string
            .map(c -> Character.toString((char) c.intValue()))
            // Collect all the letters in a list
            .collect(Collectors.toList());

        // Initialize the minimum as the biggest value to not miss any value
        int minimum = Integer.MAX_VALUE;

        // For each letter in the list
        for (String letter : letters) {
            // Remove the lowercase letter from the input
            String inputWithoutLowerCaseLetter = input.replaceAll(letter.toLowerCase(), "");
            // Remove the uppercase letter from the input
            String inputWithoutUpperCaseLetter = inputWithoutLowerCaseLetter.replaceAll(letter.toUpperCase(), "");
            // Run the reduction
            int localMinimum = Part01.recursiveReduction(new StringBuilder(inputWithoutUpperCaseLetter), inputWithoutUpperCaseLetter.length());

            // Find the minimum
            if (localMinimum < minimum) {
                minimum = localMinimum;
            }
        }

        // Print the answer
        System.out.println(String.format("The answer is: %s", minimum));
    }
}
