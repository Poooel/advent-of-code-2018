package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.Data;
import lombok.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day9_MarbleMania implements Executable {
    @Override
    public String executePartOne() {
        List<String> inputs = ChallengeHelper.readInputData(9);
        MarbleInitialParameters marbleInitialParameters = parseInput(inputs);
        return String.valueOf(
            playMarbleGame(
                marbleInitialParameters.getNumberOfMarbles(),
                marbleInitialParameters.getNumberOfPlayers(),
                1
            )
        );
    }

    @Override
    public String executePartTwo() {
        List<String> inputs = ChallengeHelper.readInputData(9);
        MarbleInitialParameters marbleInitialParameters = parseInput(inputs);
        return String.valueOf(
            playMarbleGame(
                marbleInitialParameters.getNumberOfMarbles(),
                marbleInitialParameters.getNumberOfPlayers(),
                100
            )
        );
    }

    private MarbleInitialParameters parseInput(List<String> inputs) {
        // We can get only the first line because the input is on one line
        String input = inputs.get(0);

        // Split the input on the spaces, we are interested in the number of players
        // and the value of the last marble
        // "404 players; last marble is worth 71852 points"
        // ["404", "players;", "last", "marble", "is", "worth", "71852", "points"]
        //    ↥                                                    ↥
        //   [0]                                                  [6]
        String[] splitInput = input.split(" ");

        int numberOfPlayers = Integer.parseInt(splitInput[0]);
        int numberOfMarbles = Integer.parseInt(splitInput[6]);

        return new MarbleInitialParameters(numberOfMarbles, numberOfPlayers);
    }

    private long playMarbleGame(int marbles, int players, int multiplicator) {
        // Initialize the rotating queue
        RotatingQueue<Integer> game = new RotatingQueue<>();
        // Keep the leaderboard of players
        Map<Integer, Long> leaderboard = new HashMap<>();

        // Solution from
        // https://www.reddit.com/r/adventofcode/comments/a4i97s/2018_day_9_solutions/ebepyc7

        // We add one because else there will be one iteration missing
        for (int i = 0; i < (marbles * multiplicator) + 2; i++) {
            // If i is not zero and a multiple of 23 then do special rule
            if (i != 0 && i % 23 == 0) {
                // Rotate the collection by 7 anti clock wise
                game.rotate(-7);
                // Add the score to the leaderboard for the player
                // we use merge so if there is no mapping already, it creates one
                // and put the value, else it just merge the values
                leaderboard.merge(
                    i % players,
                    i + new Long(game.pop()),
                    Long::sum
                );
                // Rotate the collection back to put it in its normal state
                game.rotate(1);
            } else {
                // Rotate the collection to insert next element
                game.rotate(1);
                // Add the element to the end of the collection
                game.push(i);
            }
        }

        // Get the highest score from the leaderboard
        return leaderboard
            // Get only the values as we don't care about the player
            .values()
            // Stream them
            .stream()
            // Create an IntStream from the Stream<Integer>
            .mapToLong(Long::longValue)
            // Get the max
            .max()
            // Get it as is it an optional
            .getAsLong();
    }

    @Value
    private class MarbleInitialParameters {
        private int numberOfMarbles;
        private int numberOfPlayers;
    }

    @Data
    private class QueueNode<T> {
        private QueueNode head;
        private QueueNode tail;
        private T value;

        QueueNode(T value) {
            this.head = null;
            this.tail = null;
            this.value = value;
        }
    }

    // Implemented my own RotatingQueue (this may not be the correct name) because
    // it appears in Java this doesn't exists. The queue only handle push, pop and rotate.
    // This is a true double linked list, each node has no other info than the previous node,
    // the next node, and the value it holds. Thus making it faster than the Java implementation
    // LinkedList.
    private class RotatingQueue<T> {
        private QueueNode<T> head;
        private QueueNode<T> tail;

        void push(T value) {
            // Create a node with the given value
            QueueNode<T> node = new QueueNode<>(value);

            // If the queue has just been created
            if (head == null && tail == null) {
                // The head and the tail are the same as there is only one element
                head = node;
                tail = node;

                head.setTail(tail);
                tail.setHead(head);
            } else if (head != null && tail != null) {
                // Set the new node's head to the current tail
                node.setHead(tail);
                // Set the tail of the tail to the new node
                tail.setTail(node);
                // Set the tail to the new node
                tail = node;
            }
        }

        T pop() {
            // Get the value of the removed node
            T value = tail.getValue();
            // Put the head of the old tail in a temp node
            QueueNode newTail = tail.getHead();

            // Dereference the old tail
            tail.setHead(null);

            // Set the new tail as the head of the previous tail
            tail = newTail;
            // The tail has no tail
            tail.setTail(null);

            // Return the value of the node removed
            return value;
        }

        /**
         * Rotate the queue with the degree given.
         * @param degrees If degrees is greater than 0 then it will rotate clockwise,
         *                if degrees is less than 0 then it will rotate anti-clockwise
         */
        void rotate(int degrees) {
            // If the list is empty then do nothing
            if (head == null && tail == null) {
                return;
            }
            if (degrees > 0) {
                for (int i = 0; i < degrees; i++) {
                    // The new head is the tail of the current head
                    QueueNode newHead = head.getTail();
                    // The new tail is the current head
                    QueueNode newTail = head;

                    // The new tail's head is the old tail
                    newTail.setHead(tail);
                    // The new tail's tail is null
                    newTail.setTail(null);

                    // The old tail's tail is now the new tail
                    tail.setTail(newTail);

                    // Assign the new tail to the tail
                    tail = newTail;

                    // The new head's head is null
                    newHead.setHead(null);

                    // Assign the new head to the head
                    head = newHead;
                }
            } else if (degrees < 0) {
                for (int i = 0; i < Math.abs(degrees); i++) {
                    // The new head is the tail of the current head
                    QueueNode newHead = tail;
                    // The new tail is the current head
                    QueueNode newTail = tail.getHead();

                    // The new head's head is null
                    newHead.setHead(null);
                    // The new head's tail is the old head
                    newHead.setTail(head);

                    // The old head's head is now the new head
                    head.setHead(newHead);

                    // Assign the new head to the head
                    head = newHead;

                    // The new tail's tail is null
                    newTail.setTail(null);

                    // Assign the new tail to the tail
                    tail = newTail;
                }
            }
        }
    }
}
