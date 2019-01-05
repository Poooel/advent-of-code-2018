package launcher;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@UtilityClass
public class ChallengeHelper {
    /**
     * Read the input data for the challenge using the day number
     * @param day The day of the challenge for the input
     * @return A list of strings corresponding to all the lines in the input file split
     *         on new line
     */
    @SneakyThrows
    public List<String> readInputData(int day) {
        // Using %02d to pad the number with one leading zero if needed
        // https://stackoverflow.com/a/35522727/7621349
        return Files.readAllLines(Paths.get(String.format("input/day_%02d.input", day)));
    }
}
