package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Day10_TheStarsAlign implements Executable {
    @Override
    public String executePartOne() {
        List<String> inputs = ChallengeHelper.readInputData(10);
        Map<Point, Velocity> initialParameters = parseInput(inputs);
        int iterationToLookAt = computeMovementOfPoints(initialParameters);
        // Recompute the set of initial parameters to draw the final state
        // because the previous computing moved the points even in the initial parameters
        // it should not have done that because we made a new instance of the array
        return drawIteration(parseInput(inputs), iterationToLookAt);
    }

    @Override
    public String executePartTwo() {
        List<String> inputs = ChallengeHelper.readInputData(10);
        Map<Point, Velocity> initialParameters = parseInput(inputs);
        // We add one to the number of iterations because, if the answer is
        // the iterations 10332, as the for loop starts at 0, it is actually the
        // iterations 10333
        return String.valueOf(computeMovementOfPoints(initialParameters) + 1);
    }

    private int computeMovementOfPoints(Map<Point, Velocity> initialParameters) {
        // Initialize the minimum variables
        int minDispersion = Integer.MAX_VALUE;
        int minDispersionIteration = Integer.MAX_VALUE;

        // Get the list of the points that will move
        List<Point> movingPoints = new ArrayList<>(initialParameters.keySet());

        // Set a default iteration number to 15000, as we saw that the answer to part 2 is
        // just above 10000, we don't need to do much more
        for (int i = 0; i < 15000; i++) {
            // Move each point with its velocity
            for (Point movingPoint : movingPoints) {
                // Had to add a uuid to each point and customized equals and hashcode because
                // has the points move, after the first iterations the velocity would be null,
                // because it couldn't find it in the map
                movingPoint.move(initialParameters.get(movingPoint));
            }

            // Compute the dispersion of points in the Y axis
            // From reddit
            // https://www.reddit.com/r/adventofcode/comments/a4skra/2018_day_10_solutions/ebi9eap
            int dispersion = computeDispersion(movingPoints);

            // Evaluate the new minimum dispersion and store the iteration it was recorded at
            // to be able to calculate this state later to visualize the words
            if (dispersion < minDispersion) {
                minDispersion = dispersion;
                minDispersionIteration = i;
            }
        }

        return minDispersionIteration;
    }

    private String drawIteration(Map<Point, Velocity> initialParameters, int iteration) {
        // Get the points that will move
        List<Point> movingPoints = new ArrayList<>(initialParameters.keySet());

        // Compute the state of the points up to the iteration we received
        // it should be the state where the words appear
        for (int i = 0; i < iteration + 1; i++) {
            for (Point movingPoint : movingPoints) {
                Velocity velocity = initialParameters.get(movingPoint);
                movingPoint.move(velocity);
            }
        }

        // Compute the minimum and maximum of both X and Y to get the width and the height
        // of the array we need to create to hold the words
        int minX = movingPoints.stream().min(Comparator.comparingInt(Point::getX)).get().getX();
        int maxX = movingPoints.stream().max(Comparator.comparingInt(Point::getX)).get().getX();

        int minY = movingPoints.stream().min(Comparator.comparingInt(Point::getY)).get().getY();
        int maxY = movingPoints.stream().max(Comparator.comparingInt(Point::getY)).get().getY();

        int width = maxX - minX;
        int height = maxY - minY;

        // We reversed the drawing by first putting the height and then the width
        // to not get it in the wrong direction
        String[][] drawing = new String[height + 1][];

        for (int i = 0; i < drawing.length; i++) {
            drawing[i] = new String[width + 1];
        }

        // Put each point in the array as an #
        for (Point point : movingPoints) {
            int x = point.getX() - minX;
            int y = point.getY() - minY;
            drawing[y][x] = "#";
        }

        // Set all other points as spaces to be able to read the result
        for (int i = 0; i < drawing.length; i++) {
            for (int j = 0; j < drawing[i].length; j++) {
                if (drawing[i][j] == null) {
                    drawing[i][j] = " ";
                }
            }
        }

        // Create a stringBuilder to be able to create the last string which will be the words
        StringBuilder stringBuilder = new StringBuilder();
        // Add a new line so the first row won't be on the launcher line
        stringBuilder.append("\n");

        // For each row in the drawing
        for (String[] strings : drawing) {
            // join it by using no character to delimit it
            String row = String.join("", strings);
            // append the row to the string builder
            stringBuilder.append(row);
            // append a new line character to be able to read the result
            stringBuilder.append("\n");
        }

        // return the representation of the string builder as a string
        return stringBuilder.toString();
    }

    private int computeDispersion(List<Point> points) {
        // To compute the dispersion, here we only need the dispersion from the Y axis,
        // so we just make the difference between the biggest Y and smalled Y, according
        // to the reddit answer
        int maxY = points.stream().max(Comparator.comparingInt(Point::getY)).get().getY();
        int minY = points.stream().min(Comparator.comparingInt(Point::getY)).get().getY();

        return maxY - minY;
    }

    private Map<Point, Velocity> parseInput(List<String> inputs) {
        Map<Point, Velocity> initialParameters = new HashMap<>();

        for (String input : inputs) {
            // Split the input on the space between the two info
            // "position=< 31159, -20533> velocity=<-3,  2>"
            //                           ↥
            // ["position=< 31159, -20533>", "velocity=<-3,  2>"]
            // to do this we use a positive lookahead in the regex
            // the lookahead is the "velocity=<-3,  2>" part and we match the space
            // before this. So the first part is about the position and the second part
            // is about the velocity
            String[] splitInput = input.split("( )(?=(velocity=<( )*(-)*\\d+,( )*(-)*\\d+>))");
            String pointAsString = splitInput[0];
            String velocityAsString = splitInput[1];
            Point point = parsePoint(pointAsString);
            Velocity velocity = parseVelocity(velocityAsString);
            initialParameters.put(point, velocity);
        }

        return initialParameters;
    }

    private Point parsePoint(String pointAsString) {
        int[] coordinates = trimBoilerplateAndSplitAndParse(pointAsString, "position");
        return new Point(coordinates[0], coordinates[1]);
    }

    private Velocity parseVelocity(String velocityAsString) {
        int[] velocities = trimBoilerplateAndSplitAndParse(velocityAsString, "velocity");
        return new Velocity(velocities[0], velocities[1]);
    }

    private int[] trimBoilerplateAndSplitAndParse(String toTrim, String boilerplate) {
        // Remove the name of the info either "position" or "velocity" and the following "=<"
        toTrim = toTrim.replace(boilerplate + "=<", "");
        // Then remove the ">", could also remove the last char from the string
        toTrim = toTrim.replace(">", "");
        // Split the result on the ","
        String[] split = toTrim.split(",");
        // Then parse the two numbers
        return new int[] { Integer.parseInt(split[0].trim()), Integer.parseInt(split[1].trim()) };
    }

    @Getter
    @Setter
    private class Point {
        private int x;
        private int y;
        private UUID id;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
            id = UUID.randomUUID();
        }

        void move(Velocity velocity) {
            this.x += velocity.getXVelocity();
            this.y += velocity.getYVelocity();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return id.equals(point.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    @Value
    private class Velocity {
        private int xVelocity;
        private int yVelocity;
    }
}
