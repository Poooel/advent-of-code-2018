package days;

import com.google.common.collect.ImmutableMap;
import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day13_MineCartMadness implements Executable {
    @Override
    public String executePartOne() {
        // Get the input from the input file day 13
        List<String> input = ChallengeHelper.readInputData(13);
        // Find the carts in the input and return the list of carts
        List<Cart> carts = findCarts(input);
        // Remove the carts from the tracks and return the tracks as char arrays
        // because it is easier to process afterward
        List<char[]> tracks = removeCarts(input, carts);
        // Find the location of the first crash and return the location of it
        Point point = findFirstCrash(tracks, carts);
        // Print the location as the result
        return String.format("%s,%s", point.getX(), point.getY());
    }

    @Override
    public String executePartTwo() {
        // Get the input from the input file day 13
        List<String> input = ChallengeHelper.readInputData(13);
        // Find the carts in the input and return the list of carts
        List<Cart> carts = findCarts(input);
        // Remove the carts from the tracks and return the tracks as char arrays
        // because it is easier to process afterward
        List<char[]> tracks = removeCarts(input, carts);
        // Find the location of the last remaining cart after the last crash and return the location of it
        Point point = findLastCrash(tracks, carts);
        // Print the location as the result
        return String.format("%s,%s", point.getX(), point.getY());
    }

    private List<Cart> findCarts(List<String> tracks) {
        // Initialize the list of carts
        List<Cart> carts = new ArrayList<>();
        // String that contains all the characters that correspond to carts on the tracks
        String cartChars = "^v><";
        // To check if a string need further processing
        Pattern pattern = Pattern.compile("[\\^v><]");

        // For each line of tracks
        for (int i = 0; i < tracks.size(); i++) {
            // If the regex doesn't match part of the string, it means there is no cart inside the string
            // so don't try to search carts in it and continue
            if (!pattern.matcher(tracks.get(i)).find()) {
                continue;
            }
            // Convert it to a char array so it is easier to get each chars
            char[] track = tracks.get(i).toCharArray();
            // For each char in the string
            for (int j = 0; j < track.length; j++) {
                // Switch depending on the char we have
                switch (cartChars.indexOf(track[j])) {
                    // If the index is 0 it means the cart is going up
                    case 0:
                        carts.add(new Cart(j, i, Direction.Up));
                        break;
                    // If the index is 1 it means the cart is going down
                    case 1:
                        carts.add(new Cart(j, i, Direction.Down));
                        break;
                    // If the index is 2 it means the cart is going right
                    case 2:
                        carts.add(new Cart(j, i, Direction.Right));
                        break;
                    // If the index is 1 it means the cart is going left
                    case 3:
                        carts.add(new Cart(j, i, Direction.Left));
                        break;
                    // If the index is other than 0, 1, 2, 3 it means it is not in the list (most probably -1)
                    default:
                        break;
                }
            }
        }

        return carts;
    }

    private List<char[]> removeCarts(List<String> tracks, List<Cart> carts) {
        // Convert the tracks from a list of strings to a list of char arrays
        // because it is easier to process when moving carts
        List<char[]> cleanTracks = new ArrayList<>();

        // For each track line
        for (int i = 0; i < tracks.size(); i++) {
            // Transform the string into a char array
            char[] cleanedTrack = tracks.get(i).toCharArray();
            // Get the cart that should be on this line
            List<Cart> cartsForLine = findCartsForLine(carts, i);

            // If there is one or more cart on this line process the line
            if (!cartsForLine.isEmpty()) {
                for (Cart cart : cartsForLine) {
                    // If the cart is going UP or DOWN, the underlying track is '|'
                    if (cart.getCurrentDirection() == Direction.Up
                        || cart.getCurrentDirection() == Direction.Down) {
                        cleanedTrack[cart.getX()] = '|';
                    // If the cart is going RIGHT or LEFT, the underlying track is '-'
                    } else if (cart.getCurrentDirection() == Direction.Left
                        || cart.getCurrentDirection() == Direction.Right) {
                        cleanedTrack[cart.getX()] = '-';
                    }
                }
            }

            // Add the track to the list of cleaned tracks
            cleanTracks.add(cleanedTrack);
        }

        return cleanTracks;
    }

    private List<Cart> findCartsForLine(List<Cart> carts, int lineNumber) {
        return carts
            // Stream the list of carts
            .stream()
            // Keep only the carts that have a Y that matches the line number
            .filter(cart -> cart.getY() == lineNumber)
            // Collect the result into a list
            .collect(Collectors.toList());
    }

    private Point findFirstCrash(List<char[]> tracks, List<Cart> carts) {
        // Infinite loop because we do not know when the "game" will finish
        while (true) {
            // Sort the carts so the first cart to be moved is the one in the top-left most corner
            // and the last one is the in the bottom-right most corner
            carts = sortCarts(carts);

            for (Cart cart : carts) {
                // Move the carts according to their underlying track
                moveCartAccordingToTrack(tracks.get(cart.getY())[cart.getX()], cart);

                // If there is a crash, stop the execution and return the location of the crash
                if (thereIsCrash(cart, carts)) {
                    return new Point(cart.getX(), cart.getY());
                }
            }
        }
    }

    private Point findLastCrash(List<char[]> tracks, List<Cart> carts) {
        // Infinite loop because we do not know when the "game" will finish
        while (true) {
            // Sort the carts so the first cart to be moved is the one in the top-left most corner
            // and the last one is the in the bottom-right most corner
            carts = sortCarts(carts);

            for (Cart cart : carts) {
                // Move the carts according to their underlying track
                moveCartAccordingToTrack(tracks.get(cart.getY())[cart.getX()], cart);
                // Flag all the carts that have crashed into each other
                flagCrashedCarts(cart, carts);
            }

            // Remove the crashed carts
            carts.removeIf(Cart::isCrashed);

            // If there is only one cart left, stop execution and return its location
            if (carts.size() == 1) {
                return new Point(carts.get(0).getX(), carts.get(0).getY());
            }
        }
    }

    private void moveCartAccordingToTrack(char track, Cart cart) {
        // If the cart is on a straight line, don't change the direction
        if (track == '|' || track == '-') {
            cart.go(cart.getCurrentDirection());
        // If the cart encouters a right turn, get the direction in which it should go
        } else if (track == '/') {
            cart.go(cart.getTurnRight().get(cart.getCurrentDirection()));
        // If the cart encouters a left turn, get the direction in which it should go
        } else if (track == '\\') {
            cart.go(cart.getTurnLeft().get(cart.getCurrentDirection()));
        // If the cart encouters an intersection
        } else if (track == '+') {
            // Get the direction in which it should go at this intersection
            Direction directionAtIntersection = cart.getDirectionAtIntersection();

            // Depending on the direction we got, if we got STRAIGHT don't change the direction
            if (directionAtIntersection == Direction.Straight) {
                cart.go(cart.getCurrentDirection());
            // If we got left, turn the cart left, this is different from a left turn track
            } else if (directionAtIntersection == Direction.Left) {
                cart.go(cart.getIntersectLeft().get(cart.getCurrentDirection()));
            // If we got right, turn the cart right, this is different from a right turn track
            } else if (directionAtIntersection == Direction.Right) {
                cart.go(cart.getIntersectRight().get(cart.getCurrentDirection()));
            }
        }
    }

    private List<Cart> sortCarts(List<Cart> carts) {
        return carts
            // Stream the list of carts
            .stream()
            // Sort it first by Y and then by X, so the first track in the list is the one with the
            // lowest Y and then the lowest X (top-left corner)
            .sorted(Comparator.comparing(Cart::getY).thenComparing(Cart::getX))
            // Collect the result into a list
            .collect(Collectors.toList());
    }

    private boolean thereIsCrash(Cart cart, List<Cart> carts) {
        // Iterate over all the cart to find if there is a crash
        for (Cart target : carts) {
            // If the target is different from the cart we are comparing to (not the same cart)
            // and they have the same coordinates, it means there is a crash, so return true
            if (!target.equals(cart) && cart.getX() == target.getX() && cart.getY() == target.getY()) {
                return true;
            }
        }

        return false;
    }

    private void flagCrashedCarts(Cart cart, List<Cart> carts) {
        for (Cart target : carts) {
            if (!target.equals(cart) && cart.getX() == target.getX() && cart.getY() == target.getY()) {
                // Same as before, instead of returning true, we mark the carts as crashed
                cart.setCrashed(true);
                target.setCrashed(true);
                break;
            }
        }
    }

    @Data
    private final class Cart {
        private int x;
        private int y;
        // Used to identify each cart
        private UUID id;
        @Getter(value = AccessLevel.PRIVATE)
        @EqualsAndHashCode.Exclude
        // The direction a cart can go into an intersection
        private final Direction[] directions = {Direction.Left, Direction.Straight, Direction.Right};
        @Getter(value = AccessLevel.PRIVATE)
        @Setter(value = AccessLevel.PRIVATE)
        @EqualsAndHashCode.Exclude
        // The inner counter for the next direction at an intersection
        private int innerCounter = 0;
        @EqualsAndHashCode.Exclude
        // The current direction the cart is going
        private Direction currentDirection;
        @EqualsAndHashCode.Exclude
        // Flag to know if the cart has crashed and should be removed
        private boolean crashed;

        // Map<Direction, Direction>
        //        ^           ^
        //  Coming from   Going to
        @EqualsAndHashCode.Exclude
        private final Map<Direction, Direction> intersectRight = ImmutableMap.of(
            Direction.Down, Direction.Left,
            Direction.Up, Direction.Right,
            Direction.Left, Direction.Up,
            Direction.Right, Direction.Down
        );

        @EqualsAndHashCode.Exclude
        private final Map<Direction, Direction> intersectLeft = ImmutableMap.of(
            Direction.Down, Direction.Right,
            Direction.Up, Direction.Left,
            Direction.Left, Direction.Down,
            Direction.Right, Direction.Up
        );

        // /
        @EqualsAndHashCode.Exclude
        private final Map<Direction, Direction> turnRight = ImmutableMap.of(
            Direction.Down, Direction.Left,
            Direction.Up, Direction.Right,
            Direction.Left, Direction.Down,
            Direction.Right, Direction.Up
        );

        // \
        @EqualsAndHashCode.Exclude
        private final Map<Direction, Direction> turnLeft = ImmutableMap.of(
            Direction.Down, Direction.Right,
            Direction.Up, Direction.Left,
            Direction.Left, Direction.Up,
            Direction.Right, Direction.Down
        );

        Cart(int x, int y, Direction direction) {
            this.x = x;
            this.y = y;
            this.id = UUID.randomUUID();
            this.currentDirection = direction;
        }

        Direction getDirectionAtIntersection() {
            // If the inner counter is equal to the directions' lenght
            // then set the counter to 0
            if (innerCounter == directions.length) {
                innerCounter = 0;
            }

            // Get the direction with the inner counter then increment the counter
            // So we will get the directions in a loop
            return directions[innerCounter++];
        }

        void go(Direction direction) {
            switch (direction) {
                case Left:
                    this.currentDirection = Direction.Left;
                    this.x -= 1;
                    break;
                case Right:
                    this.currentDirection = Direction.Right;
                    this.x += 1;
                    break;
                case Straight:
                    break;
                case Down:
                    this.currentDirection = Direction.Down;
                    this.y += 1;
                    break;
                case Up:
                    this.currentDirection = Direction.Up;
                    this.y -= 1;
                    break;
            }
        }
    }

    @Value
    private final class Point {
        private int x;
        private int y;
    }

    private enum Direction {
        Left,
        Right,
        Straight,
        Down,
        Up
    }
}
