package day.one.chronal.calibration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PartOne
{
    public static void main(String [ ] args) throws IOException
    {
        System.out.println(String.format("The answer is %s",
                // Get all the number in the file,
                // as a list of string split on new line separator
                Files.readAllLines(Paths.get("data/day-one.data"))
                        // Stream it
                        .stream()
                        // Transform the stream into an IntStream by parsing the string
                        .mapToInt(Integer::parseInt)
                        // Make the sum of it
                        .sum()
                )
        );
    }
}
