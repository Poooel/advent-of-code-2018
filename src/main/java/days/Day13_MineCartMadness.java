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
import java.util.stream.Collectors;

public class Day13_MineCartMadness implements Executable {
    @Override
    public String executePartOne() {
        List<String> input = ChallengeHelper.readInputData(13);
        List<Cart> carts = findCarts(input);
        List<char[]> tracks = removeCarts(input);
        Point point = findFirstCrash(tracks, carts);
        return String.format("%s,%s", point.getX(), point.getY());
    }

    @Override
    public String executePartTwo() {
        List<String> input = ChallengeHelper.readInputData(13);
        List<Cart> carts = findCarts(input);
        List<char[]> tracks = removeCarts(input);
        Point point = findLastCrash(tracks, carts);
        return String.format("%s,%s", point.getX(), point.getY());
    }

    private List<Cart> findCarts(List<String> tracks) {
        List<Cart> carts = new ArrayList<>();
        String cartChars = "^v><";

        for (int i = 0; i < tracks.size(); i++) {
            for (int j = 0; j < tracks.get(i).toCharArray().length; j++) {
                switch (cartChars.indexOf(tracks.get(i).toCharArray()[j])) {
                    case 0:
                        carts.add(new Cart(j, i, Direction.Up));
                        break;
                    case 1:
                        carts.add(new Cart(j, i, Direction.Down));
                        break;
                    case 2:
                        carts.add(new Cart(j, i, Direction.Right));
                        break;
                    case 3:
                        carts.add(new Cart(j, i, Direction.Left));
                        break;
                    default:
                        break;
                }
            }
        }

        return carts;
    }

    private List<char[]> removeCarts(List<String> tracks) {
        List<char[]> cleanTracks = new ArrayList<>();
        String cartChars = "^v><";

        for (String track : tracks) {
            char[] cleanedTrack = track.toCharArray();
            for (int j = 0; j < cleanedTrack.length; j++) {
                switch (cartChars.indexOf(track.toCharArray()[j])) {
                    case 0:
                        cleanedTrack[j] = '|';
                        break;
                    case 1:
                        cleanedTrack[j] = '|';
                        break;
                    case 2:
                        cleanedTrack[j] = '-';
                        break;
                    case 3:
                        cleanedTrack[j] = '-';
                        break;
                    default:
                        break;
                }
            }

            cleanTracks.add(cleanedTrack);
        }

        return cleanTracks;
    }

    private Point findFirstCrash(List<char[]> tracks, List<Cart> carts) {
        Point point = null;

        while (point == null) {
            carts = sortCarts(carts);

            for (Cart cart : carts) {
                moveCartAccordingToTrack(tracks.get(cart.getY())[cart.getX()], cart);

                if (thereIsCrash(cart, carts)) {
                    point = new Point(cart.getX(), cart.getY());
                    break;
                }
            }
        }

        return point;
    }

    private Point findLastCrash(List<char[]> tracks, List<Cart> carts) {
        Point point = null;

        while (point == null) {
            carts = sortCarts(carts);
            List<Cart> crashedCarts = new ArrayList<>();

            for (Cart cart : carts) {
                moveCartAccordingToTrack(tracks.get(cart.getY())[cart.getX()], cart);
                removedCrashedCarts(cart, carts, crashedCarts);
            }

            carts.removeAll(crashedCarts);
            crashedCarts.clear();

            if (carts.size() == 1) {
                point = new Point(carts.get(0).getX(), carts.get(0).getY());
            }
        }

        return point;
    }

    private void moveCartAccordingToTrack(char track, Cart cart) {
        if (track == '|' || track == '-') {
            cart.go(cart.getCurrentDirection());
        } else if (track == '/') {
            cart.go(cart.getTurnRight().get(cart.getCurrentDirection()));
        } else if (track == '\\') {
            cart.go(cart.getTurnLeft().get(cart.getCurrentDirection()));
        } else if (track == '+') {
            Direction directionAtIntersection = cart.getDirectionAtIntersection();

            if (directionAtIntersection == Direction.Straight) {
                cart.go(cart.getCurrentDirection());
            } else if (directionAtIntersection == Direction.Left) {
                cart.go(cart.getIntersectLeft().get(cart.getCurrentDirection()));
            } else if (directionAtIntersection == Direction.Right) {
                cart.go(cart.getIntersectRight().get(cart.getCurrentDirection()));
            }
        }
    }

    private List<Cart> sortCarts(List<Cart> carts) {
        return carts
            .stream()
            .sorted(Comparator.comparing(Cart::getY).thenComparing(Cart::getX))
            .collect(Collectors.toList());
    }

    private boolean thereIsCrash(Cart cart, List<Cart> carts) {
        for (Cart target : carts) {
            if (target != cart && cart.getX() == target.getX() && cart.getY() == target.getY()) {
                return true;
            }
        }

        return false;
    }

    private void removedCrashedCarts(Cart cart, List<Cart> carts, List<Cart> crashedCarts) {
        for (Cart target : carts) {
            if (target != cart && cart.getX() == target.getX() && cart.getY() == target.getY()) {
                crashedCarts.add(cart);
                crashedCarts.add(target);
                break;
            }
        }
    }

    @Data
    private final class Cart {
        private int x;
        private int y;
        private UUID id;
        @Getter(value = AccessLevel.PRIVATE)
        @EqualsAndHashCode.Exclude
        private final Direction[] directions = {Direction.Left, Direction.Straight, Direction.Right};
        @Getter(value = AccessLevel.PRIVATE)
        @Setter(value = AccessLevel.PRIVATE)
        @EqualsAndHashCode.Exclude
        private int innerCounter = 0;
        @EqualsAndHashCode.Exclude
        private Direction currentDirection;
        @EqualsAndHashCode.Exclude
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
            if (innerCounter == directions.length) {
                innerCounter = 0;
            }

            return directions[innerCounter++];
        }

        void go(Direction direction) {
            switch (direction) {
                case Left:
                    goLeft();
                    break;
                case Right:
                    goRight();
                    break;
                case Straight:
                    break;
                case Down:
                    goDown();
                    break;
                case Up:
                    goUp();
                    break;
            }
        }

        private void goUp() {
            this.currentDirection = Direction.Up;
            this.y -= 1;
        }

        private void goDown() {
            this.currentDirection = Direction.Down;
            this.y += 1;
        }

        private void goRight() {
            this.currentDirection = Direction.Right;
            this.x += 1;
        }

        private void goLeft() {
            this.currentDirection = Direction.Left;
            this.x -= 1;
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
