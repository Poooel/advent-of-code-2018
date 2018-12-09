package day_5_alchemical_reduction;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Part01 {
    public static void main(String[] args) throws Exception {
        // Get the line in the file, as day 5 input is only one line
        List<String> inputs = Files.readAllLines(Paths.get("input/day_05.input"));
        // Transform the string into a stringBuilder so it is easier to manipulate
        StringBuilder input = new StringBuilder(inputs.get(0));
        System.out.println(String.format("The answer is: %s", recursiveReduction(input, input.length())));
    }

    public static int recursiveReduction(StringBuilder toReduct, int size) {
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

    public static boolean areSameTypeButOpposingPolarity(Character c1, Character c2) {
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
