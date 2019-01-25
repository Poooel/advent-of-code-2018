package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.Value;

import java.util.List;

public class Day11_ChronalCharge implements Executable {
    @Override
    public String executePartOne() {
        List<String> inputs = ChallengeHelper.readInputData(11);
        int gridSerialNumber = parseInput(inputs);
        int[][] powerGrid = initializePowerGrid(gridSerialNumber);
        SumContext sumContext = findBiggestArea(powerGrid, 3, 4);
        return String.format("%d,%d", sumContext.getX(), sumContext.getY());
    }

    @Override
    public String executePartTwo() {
        List<String> inputs = ChallengeHelper.readInputData(11);
        int gridSerialNumber = parseInput(inputs);
        int[][] powerGrid = initializePowerGrid(gridSerialNumber);
        SumContext sumContext = findBiggestArea(powerGrid, 0, powerGrid.length);
        return String.format("%d,%d,%d", sumContext.getX(), sumContext.getY(), sumContext.getSize());
    }

    private int parseInput(List<String> inputs) {
        // Take the first element in the list and parse it as an int, because we know the
        // input for this day is only an integer
        return Integer.parseInt(inputs.get(0));
    }

    private int[][] initializePowerGrid(int gridSerialNumber) {
        // Initialize a two-dimensional array of size 300 for the power grid
        int[][] powerGrid = new int[300][];

        // For each row
        for (int y = 0; y < powerGrid.length; y++) {
            // Create a new row of the same size
            powerGrid[y] = new int[powerGrid.length];

            // Initialize the power level of each cell
            for (int x = 0; x < powerGrid[y].length; x++) {
                // Compute the rack id because we need it twice
                int rackId = x + 10;
                // Compute power level following algorithm
                powerGrid[y][x] = getHundredDigit(((rackId * y) + gridSerialNumber) * rackId) - 5;
            }
        }

        return powerGrid;
    }

    private int getHundredDigit(int number) {
        String numberAsString = String.valueOf(number);
        // Get the third digit going from the end so we always get the hundred digit
        return Integer.parseInt(Character.toString(numberAsString.charAt(numberAsString.length() - 3)));
    }

    private SumContext findBiggestArea(int[][] powerGrid, int lowerBoundSize, int upperBoundSize) {
        int maximumPower = Integer.MIN_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxSize = Integer.MIN_VALUE;

        // The loop to test all the sizes, upperBound is exclusive
        for (int s = lowerBoundSize; s < upperBoundSize; s++) {
            // The y
            for (int i = 0; i < powerGrid.length - s; i++) {
                // The x
                for (int j = 0; j < powerGrid[i].length - s; j++) {
                    // Compute the sum
                    int sumOfSquare = sumOfSquare(powerGrid, j, i, s);
                    // If the sum greater than maximum power
                    // then store infos
                    if (sumOfSquare > maximumPower) {
                        maximumPower = sumOfSquare;
                        maxX = j;
                        maxY = i;
                        maxSize = s;
                    }
                }
            }
        }

        return new SumContext(maxX, maxY, maxSize);
    }

    private int sumOfSquare(int[][] powerGrid, int currentX, int currentY, int squareSize) {
        int sum = 0;

        // As we compute for a square, use the same offset for x and y
        // Also don't check for IndexOutOfBound because we take care of this
        // in the caller method
        for (int i = 0; i < squareSize; i++) {
            for (int j = 0; j < squareSize; j++) {
                sum += powerGrid[currentY + i][currentX + j];
            }
        }

        return sum;
    }

    @Value
    private final class SumContext {
        private int x;
        private int y;
        private int size;
    }
}
