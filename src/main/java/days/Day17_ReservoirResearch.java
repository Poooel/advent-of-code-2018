package days;

import com.google.common.primitives.Chars;
import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day17_ReservoirResearch implements Executable {
    @Override
    public String executePartOne() {
        List<String> input = ChallengeHelper.readInputData(17);
        char[][] map = parseInput(input);
        return null;
    }

    @Override
    public String executePartTwo() {
        return null;
    }

    private char[][] parseInput(List<String> input) {
        int lowestX = findLowestX(input);
        int biggestX = findBiggestX(input);

        int width = ((biggestX - lowestX) + 1) + 2;
        int height = findBiggestY(input) + 1;
        int offset = lowestX - 1;

        char[][] map = initializeMap(width, height);

        Slice slice = parseSliceInfo(input);

        map = placeClay(map, slice, offset);
        map = placeWaterSource(offset, map);

        return map;
    }

    private char[][] initializeMap(int width, int height) {
        char[][] map = new char[height][];

        for (int i = 0; i < height; i++) {
            map[i] = new char[width];
            Arrays.fill(map[i], '.');
        }

        return map;
    }

    private char[][] placeClay(char[][] map, Slice slice, int offset) {
        for (Scan scan : slice.getScans()) {
            if (scan.getOrigin() == 'x') {
                for (int i = 0; i < scan.getRange().length(); i++) {
                    map[i + scan.getRange().getStart()][scan.getStart() - offset] = '#';
                }
            } else {
                for (int i = 0; i < scan.getRange().length(); i++) {
                    map[scan.getStart()][(scan.getRange().getStart() - offset) + i] = '#';
                }
            }
        }

        return map;
    }

    private char[][] placeWaterSource(int offset, char[][] map) {
        map[0][500 - offset] = '+';
        return map;
    }

    private Slice parseSliceInfo(List<String> input) {
        List<Scan> scans = new ArrayList<>();

        for (String line : input) {
            String[] content = line.split(", ");

            int start = Integer.parseInt(content[0].split("=")[1]);
            char origin = content[0].split("=")[0].charAt(0);

            String[] range = content[1].split("=");

            range = range[1].split("[.]{2}");

            scans.add(
                new Scan(
                    start,
                    origin,
                    new Range(
                        Integer.parseInt(range[0]),
                        Integer.parseInt(range[1])
                    )
                )
            );
        }

        return new Slice(scans);
    }

    private int findLowestX(List<String> input) {
        return getTheXs(input).min().orElse(Integer.MIN_VALUE);
    }

    private int findBiggestX(List<String> input) {
        return getTheXs(input).max().orElse(Integer.MAX_VALUE);
    }

    private int findBiggestY(List<String> input) {
        return getTheYs(input).max().orElse(Integer.MAX_VALUE);
    }

    private IntStream getTheXs(List<String> input) {
        return input
            .stream()
            .filter(line -> line.charAt(0) == 'x')
            .map(line -> line.split(", ")[0])
            .map(line -> line.substring(2).trim())
            .mapToInt(Integer::parseInt);
    }

    private IntStream getTheYs(List<String> input) {
        return input
            .stream()
            .filter(line -> line.charAt(0) == 'y')
            .map(line -> line.split(", ")[0])
            .map(line -> line.substring(2).trim())
            .mapToInt(Integer::parseInt);
    }

    private char[][] simulateFlowOfWater(char[][] map) {
        int hashCode = hashCode(map);

        while (hashCode(map) != hashCode) {
            boolean hasStalled = false;
            boolean isOutOfBounds = false;
            Coordinates water = generateWaterFromSpring(map);
            Coordinates comingFrom = water.copy();

            water = water.down();

            while (!hasStalled || !isOutOfBounds) {
                if (water.getY() >= map.length) {
                    isOutOfBounds = true;
                }

                if (map[water.getY()][water.getX()] == '.') {
                    map = putWaterPath(comingFrom, map);
                    comingFrom = water.copy();
                    water = water.down();
                } else if (map[water.getY()][water.getX()] == '#') {
                    if (water.getY() - comingFrom.getY() == 1) {
                        map = putWaterPath(comingFrom, map);
                        comingFrom = water.copy();
                        water = water.left();
                    } else if (water.getX() - comingFrom.getX() == 1) {
                        map = putWaterPath(comingFrom, map);
                        comingFrom = water.copy();

                    }
                }
            }

            hashCode = hashCode(map);
        }
    }

    private char[][] putWaterPath(Coordinates coordinates, char[][] map) {
        map[coordinates.getY()][coordinates.getX()] = '|';
        return map;
    }

    private char[][] putStalledWater(Coordinates coordinates, char[][] map) {
        map[coordinates.getY()][coordinates.getX()] = '~';
        return map;
    }

    private Coordinates generateWaterFromSpring(char[][] map) {
        return new Coordinates(
            Chars.indexOf(map[0], '+'),
            1
        );
    }

    private int hashCode(char[][] map) {
        int hashCode = 0;

        for (char[] row : map) {
            hashCode += Arrays.hashCode(row);
        }

        return hashCode;
    }

    @Value
    private final class Scan {
        private int start;
        private char origin;
        private Range range;
    }

    @Value
    private final class Slice {
        private List<Scan> scans;
    }

    @Value
    private final class Range {
        private int start;
        private int end;

        int length() {
            return (end - start) + 1;
        }
    }

    @Value
    private final class Coordinates {
        private int x;
        private int y;

        Coordinates copy() {
            return new Coordinates(this.getX(), this.getY());
        }

        Coordinates down() {
            return new Coordinates(this.getX(), this.getY() + 1);
        }

        Coordinates left() {
            return new Coordinates(this.getX() + 1, this.getY());
        }

        Coordinates right() {
            return new Coordinates(this.getX() - 1, this.getY());
        }
    }

    private void debug(char[][] map) {
        for (char[] line : map) {
            System.out.println(new String(line));
        }
    }
}
