package day.two.inventory.management.system;

import com.google.common.base.CharMatcher;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PartOne
{
    public static void main(String [ ] args) throws Exception
    {
        List<String> inputs = Files.readAllLines(Paths.get("data/day-two.data"));
        int twos = 0;
        int threes = 0;

        for (String input : inputs) {
            List<Character> excluded = new ArrayList<>();
            boolean twosFlag = false;
            boolean threesFlag = false;

            for (char c : input.toCharArray()) {
                if (excluded.contains(c)) {
                    continue;
                }
                int count = CharMatcher.is(c).countIn(input);
                excluded.add(c);
                if (count == 2) {
                    twosFlag = true;
                } else if (count == 3) {
                    threesFlag = true;
                }
            }

            if (twosFlag) {
                twos++;
            }

            if (threesFlag) {
                threes++;
            }
        }

        System.out.println(String.format("The answer is %s", twos * threes));
    }
}
