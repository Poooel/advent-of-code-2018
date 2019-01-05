package launcher;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@UtilityClass
class LauncherHelper {
    private boolean isInRange(int number, int lowerBound, int upperBound) {
        return number >= lowerBound && number <= upperBound;
    }

    private boolean isInteger(String input) {
        try {
            // Try to parse the input to an integer
            // if it fails, then this is not an integer
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @SneakyThrows
    int getValidIntegerInput(String retryMessage, int lowerBound, int upperBound) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        String input = bufferedReader.readLine();

        while (!isInteger(input) || !isInRange(Integer.parseInt(input), lowerBound, upperBound)) {
            System.out.println(retryMessage);
            input = bufferedReader.readLine();
        }

        return Integer.parseInt(input);
    }
}
