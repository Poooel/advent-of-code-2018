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

        while (true) {
            for (String number : input) {
                frequency += Integer.parseInt(number);

                if (frequencies.containsKey(frequency)) {
                    throw new Exception(String.format("The answer is %s", frequency));
                }

                frequencies.put(frequency, frequency);
            }
        }
    }
}
