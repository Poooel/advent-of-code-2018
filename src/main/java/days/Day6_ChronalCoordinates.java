package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Day6_ChronalCoordinates implements Executable {
    @Override
    public String executePartOne() {
        List<String> inputs = ChallengeHelper.readInputData(6);
        // Parse the list of string to coordinates
        List<Point> points = parseCoordinates(inputs);
        Point[][] map = computeCoordinates(points);
        map = excludeInfiniteAreas(map);

        return String.valueOf(findLargestArea(map));
    }

    @Override
    public String executePartTwo() {
        List<String> inputs = ChallengeHelper.readInputData(6);
        // Parse the list of string to coordinates
        List<Point> points = parseCoordinates(inputs);
        boolean[][] map =  computeSafeArea(points);

        return String.valueOf(findLargestArea(map));
    }

    private List<Point> parseCoordinates(List<String> inputs) {
        return inputs
            // Stream the list of input
            .stream()
            // Split the line on the "," the first part is the 'x' the second part is the 'y'
            .map(input -> input.split(","))
            // Parse the string to an int and create a point
            .map(splitInput -> new Point(
                Integer.parseInt(splitInput[0].trim()),
                Integer.parseInt(splitInput[1].trim())
                // Collect the result into a list
            )).collect(Collectors.toList());
    }

    private int findBiggestX(List<Point> points) {
        return points
            // Stream the list of points
            .stream()
            // Indicate to the max function we want to find the max for the X property of the Point
            .max(Comparator.comparingInt(Point::getX))
            // We can get it without isPresent because we know the stream is not empty
            .get()
            // Get the biggest X in the stream
            .getX();
    }

    private int findBiggestY(List<Point> points) {
        return points
            // Stream the list of points
            .stream()
            // Indicate to the max function we want to find the max for the Y property of the Point
            .max(Comparator.comparingInt(Point::getY))
            // We can get it without isPresent because we know the stream is not empty
            .get()
            // Get the biggest Y in the stream
            .getY();
    }

    private Point[][] initializeMap(List<Point> points) {
        int width = findBiggestX(points);
        int height = findBiggestY(points);

        Point[][] map = new Point[width][];

        for (int i = 0; i < width; i++) {
            map[i] = new Point[height];
        }

        return map;
    }

    private Point[][] computeCoordinates(List<Point> points) {
        Point[][] map = initializeMap(points);

        List<Distance> distances = new ArrayList<>();

        for (int i  = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                Point currentPoint = new Point(i, j);

                for (Point point : points) {
                    distances.add(
                        new Distance(
                            point,
                            computeManhattanDistance(currentPoint, point)
                        )
                    );
                }

                if (isMinValuePresentMoreThanOnce(distances)) {
                    map[i][j] = null;
                } else {
                    map[i][j] = findMinDistance(distances).getPoint();
                }

                distances.clear();
            }
        }

        return map;
    }

    private boolean isMinValuePresentMoreThanOnce(List<Distance> distances) {
        return distances
            .stream()
            .filter(item -> item.getValue() == findMinDistance(distances).getValue())
            .collect(Collectors.toList())
            .size() > 1;
    }

    private Distance findMinDistance(List<Distance> distances) {
        return distances
            .stream()
            .min(Comparator.comparingInt(Distance::getValue))
            .get();
    }

    private int computeManhattanDistance(Point a, Point b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private Point[][] excludeInfiniteAreas(Point[][] map) {
        // We can assume that any area on a side of the map will be infinite
        List<Point> pointsExcluded = new ArrayList<>();

        // Getting top side
        for (Point[] points : map) {
            pointsExcluded.add(points[0]);
        }

        // Getting bottom side
        for (Point[] points : map) {
            pointsExcluded.add(points[points.length - 1]);
        }

        // Getting right side
        Collections.addAll(pointsExcluded, map[map.length - 1]);

        // Getting left side
        Collections.addAll(pointsExcluded, map[0]);

        // Remove duplicates
        pointsExcluded = pointsExcluded.stream().distinct().collect(Collectors.toList());

        // Remove points from map
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (pointsExcluded.contains(map[i][j])) {
                    map[i][j] = null;
                }
            }
        }

        return map;
    }

    private int findLargestArea(Point[][] map) {
        Map<Point, Integer> areas = new HashMap<>();

        for (Point[] points : map) {
            for (Point point : points) {
                // https://stackoverflow.com/a/42648785/7621349
                areas.merge(point, 1, Integer::sum);
            }
        }

        areas.remove(null);

        return Collections.max(areas.values());
    }

    private boolean[][] computeSafeArea(List<Point> points) {
        boolean[][] map = initializeSafeMap(points);

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                Point currentPoint = new Point(i, j);
                int sumOfDistances = 0;

                for (Point point : points) {
                    sumOfDistances += computeManhattanDistance(currentPoint, point);
                }

                map[i][j] = sumOfDistances < 10000;
            }
        }

        return map;
    }

    private boolean[][] initializeSafeMap(List<Point> points) {
        int width = findBiggestX(points);
        int height = findBiggestY(points);

        boolean[][] map = new boolean[width][];

        for (int i = 0; i < width; i++) {
            map[i] = new boolean[height];
        }

        return map;
    }

    private int findLargestArea(boolean[][] map) {
        int area = 0;

        for (boolean[] safeRow : map) {
            for (boolean isSafe : safeRow) {
                if (isSafe) {
                    area++;
                }
            }
        }

        return area;
    }

    @Value
    private class Distance {
        private Point point;
        private int value;
    }

    @Value
    private class Point {
        private int x;
        private int y;
    }
}
