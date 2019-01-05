package days;

import launcher.ChallengeHelper;
import launcher.Executable;

import java.util.List;
import java.util.stream.Collectors;

public class Day5_AlchemicalReduction implements Executable {
    @Override
    public String executePartOne() {
        List<String> inputs = ChallengeHelper.readInputData(5);
        // Transform the string into a stringBuilder so it is easier to manipulate
        StringBuilder input = new StringBuilder(inputs.get(0));

        return String.valueOf(recursiveReduction(input, input.length()));
    }

    @Override
    public String executePartTwo() {
        List<String> inputs = ChallengeHelper.readInputData(5);
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
            String inputWithoutLowerCaseLetter =
                input.replaceAll(letter.toLowerCase(), "");
            // Remove the uppercase letter from the input
            String inputWithoutUpperCaseLetter =
                inputWithoutLowerCaseLetter.replaceAll(letter.toUpperCase(), "");
            // Run the reduction
            int localMinimum = recursiveReduction(
                new StringBuilder(inputWithoutUpperCaseLetter),
                inputWithoutUpperCaseLetter.length()
            );

            // Find the minimum
            if (localMinimum < minimum) {
                minimum = localMinimum;
            }
        }

        return String.valueOf(minimum);
    }

    private int recursiveReduction(StringBuilder toReduct, int size) {
        // For each char in the string
        for (int i = 0; i < toReduct.length(); i++) {
            // Avoid IndexOutOfBoundsException
            if (i < toReduct.length() - 2) {
                // If the two char are of equal type and inverse polarity e.g aA, Aa
                if (areSameTypeButOpposingPolarity(toReduct.charAt(i), toReduct.charAt(i + 1))) {
                    // Delete the two chars
                    // Upper bound is exclusive this is why we put + 2
                    toReduct.delete(i, i + 2);
                }
            }
        }
        // If we didn't remove any char return the size of the string builder
        // we don't need to compute it, because it is the same as the previous call
        // which already computed it when calling it recursively
        if (size == toReduct.length()) {
            return size;
        }
        // Else call the function recursively
        return recursiveReduction(toReduct, toReduct.length());
    }

    private boolean areSameTypeButOpposingPolarity(Character c1, Character c2) {
        // If the two chars are the same letter, we lower case them, because they can be of
        // different case and we check that they are indeed the same char
        if (Character.toLowerCase(c1) == Character.toLowerCase(c2)) {
            // first if checks: Aa
            if (Character.isUpperCase(c1) && Character.isLowerCase(c2)) {
                return true;
                // second if checks: aA
            } else if (Character.isLowerCase(c1) && Character.isUpperCase(c2)) {
                return true;
            }
        }
        // if it matched nothing return false
        return false;
    }
}
