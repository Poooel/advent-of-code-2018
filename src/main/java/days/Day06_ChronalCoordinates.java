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

public class Day06_ChronalCoordinates implements Executable {
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
        return String.valueOf(
            computeSafeArea(
                parseCoordinates(
                    ChallengeHelper.readInputData(6)
                )
            )
        );
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
        // The width is the biggest X we found in the coordinates
        int width = findBiggestX(points);
        // The height is the biggest Y we found in the coordinates
        // this is to avoid doing unnecessary work when computing
        // area, if the map is bigger than needed
        int height = findBiggestY(points);

        // The map is a 2 dimensional array of points
        Point[][] map = new Point[width][];

        for (int i = 0; i < width; i++) {
            map[i] = new Point[height];
        }

        return map;
    }

    private Point[][] computeCoordinates(List<Point> points) {
        // Initialize the map
        Point[][] map = initializeMap(points);

        // Create a new list to hold the distances
        List<Distance> distances = new ArrayList<>();

        for (int i  = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                // Save the current point
                Point currentPoint = new Point(i, j);

                // For each points, compute the distance from the current point to the
                // given point and add it to the list
                for (Point point : points) {
                    distances.add(
                        new Distance(
                            point,
                            computeManhattanDistance(currentPoint, point)
                        )
                    );
                }

                // If the min distance is present more than once in the list
                // it means the points is equally far from two points in the list of input
                // so we put a null. If the point is present only once then put the corresponding
                // point in the map
                if (isMinValuePresentMoreThanOnce(distances)) {
                    map[i][j] = null;
                } else {
                    map[i][j] = findMinDistance(distances).getPoint();
                }

                // Clear the distances as we start another point on the map
                distances.clear();
            }
        }

        return map;
    }

    private boolean isMinValuePresentMoreThanOnce(List<Distance> distances) {
        return distances
            // Stream the list of distances
            .stream()
            // Filter the item that have the same value
            .filter(item -> item.getValue() == findMinDistance(distances).getValue())
            // Collect them into a list
            .collect(Collectors.toList())
            // If the size of this list is bigger than one, then one value is present
            // more than one time
            .size() > 1;
    }

    private Distance findMinDistance(List<Distance> distances) {
        return distances
            // Stream the list of distances
            .stream()
            // Find the min in the list using the getValue of the distance object
            .min(Comparator.comparingInt(Distance::getValue))
            // Get it as min return an Optional
            .get();
    }

    private int computeManhattanDistance(Point a, Point b) {
        // https://en.wikipedia.org/wiki/Taxicab_geometry
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

        // Remove the null key from the areas map, it corresponds to the points
        // we excluded earlier or two equally distant points
        areas.remove(null);

        return Collections.max(areas.values());
    }

    private int computeSafeArea(List<Point> points) {
        // We don't need to actually compute the map this time
        // so we just use a counter to keep track of the safe area
        int safeZone = 0;

        for (int i = 0; i < findBiggestX(points); i++) {
            for (int j = 0; j < findBiggestY(points); j++) {
                Point currentPoint = new Point(i, j);
                int sumOfDistances = 0;

                for (Point point : points) {
                    sumOfDistances += computeManhattanDistance(currentPoint, point);
                }

                // if the sum of distances to this point is less than 10000 then
                // increment safe zone counter
                if (sumOfDistances < 10000) {
                    safeZone++;
                }
            }
        }

        return safeZone;
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
