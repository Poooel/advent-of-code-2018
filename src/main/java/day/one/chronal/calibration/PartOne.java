package day.one.chronal.calibration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PartOne
{
    public static void main(String [ ] args) throws IOException
    {
        System.out.println(String.format("The answer is %s",
                Files.readAllLines(Paths.get("data/day-one.data"))
                .stream()
                .mapToInt(Integer::parseInt)
                .sum())
        );
    }
}
