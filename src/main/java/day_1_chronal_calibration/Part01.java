package day_1_chronal_calibration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Part01 {
    public static void main(String[] args) throws IOException {
        System.out.println(String.format("The answer is %s",
                // Get all the number in the file,
                // as a list of string split on new line separator
                Files.readAllLines(Paths.get("input/day_01.input"))
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
