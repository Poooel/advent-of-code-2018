package day.two.inventory.management.system;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PartTwo
{
    public static void main(String [ ] args) throws Exception
    {
        List<String> inputs = Files.readAllLines(Paths.get("data/day-two.data"));
        int stringIndex = 0;
        int charIndex = 0;
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
        StringBuilder sb = new StringBuilder(inputs.get(stringIndex));
        sb.deleteCharAt(charIndex);
        // Print the StringBuilder as a string
        System.out.println(String.format("The answer is %s", sb.toString()));
    }
}
