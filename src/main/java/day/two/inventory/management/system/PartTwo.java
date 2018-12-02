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

        for (String input : inputs) {
            boolean isSet = false;
            for (String nestedInput : inputs) {
                if (nestedInput.equals(input)) {
                    continue;
                }

                int errors = 0;

                for (int i = 0; i < nestedInput.length(); i++) {
                    if (input.charAt(i) == nestedInput.charAt(i)) {
                        continue;
                    } else {
                        errors++;
                        charIndex = i;
                    }

                    if (errors > errorThreshold) {
                        break;
                    }
                }

                if (errors == errorThreshold) {
                    stringIndex = inputs.indexOf(input);
                    isSet = true;
                    break;
                }
            }

            if (isSet) {
                break;
            }
        }

        StringBuilder sb = new StringBuilder(inputs.get(stringIndex));
        sb.deleteCharAt(charIndex);
        System.out.println(String.format("The answer is %s", sb.toString()));
    }
}
