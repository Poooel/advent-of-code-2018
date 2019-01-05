package days;

import com.google.common.base.CharMatcher;
import launcher.ChallengeHelper;
import launcher.Executable;

import java.util.ArrayList;
import java.util.List;

public class InventoryManagementSystem implements Executable {
    @Override
    public String executePartOne() {
        List<String> inputs = ChallengeHelper.readInputData(2);
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

        return String.valueOf(twos * threes);
    }

    @Override
    public String executePartTwo() {
        List<String> inputs = ChallengeHelper.readInputData(2);
        // String index to find the string for the result
        int stringIndex = 0;
        // The char to remove for the result
        int charIndex = 0;
        // The number of errors we want between the strings
        int errorThreshold = 1;

        // For all the strings we got in the input
        for (String input : inputs) {
            // We use this flag to break execution later
            // to avoid uneccessary work
            boolean isSet = false;
            // We need to compare the current string to all others
            // so we do the same loop
            for (String nestedInput : inputs) {
                // if this is the same string don't bother computing it
                // and continue
                if (nestedInput.equals(input)) {
                    continue;
                }

                // Counter for the number of errors between the 2 strings
                int errors = 0;

                // Iterate over the chars of the string
                for (int i = 0; i < nestedInput.length(); i++) {
                    // If the chars are equal, continue as we are not interested
                    // in equal chars
                    if (input.charAt(i) == nestedInput.charAt(i)) {
                        continue;
                    } else {
                        // If they are not equal, increment the error counter
                        // and save the index of the char
                        errors++;
                        charIndex = i;
                    }

                    // If the error counter is greather than the error threshold
                    // don't bother continuing and break
                    if (errors > errorThreshold) {
                        break;
                    }
                }

                // If when we got here, there is only one error (errorThreshold is set to 1)
                // then it means we found our strings
                if (errors == errorThreshold) {
                    // Save the index of the string, input or nestedInput is the same
                    // as the two strings should be nearly identical
                    stringIndex = inputs.indexOf(input);
                    // Set the flag to true to halt execution of outter loop
                    isSet = true;
                    // And break this one
                    break;
                }
            }

            // If the flag is true break this loop to return result
            if (isSet) {
                break;
            }
        }

        // Using a StringBuilder to remove the char at the given index easily
        StringBuilder stringBuilder = new StringBuilder(inputs.get(stringIndex));
        stringBuilder.deleteCharAt(charIndex);

        return stringBuilder.toString();
    }
}
