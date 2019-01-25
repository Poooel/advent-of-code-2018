package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.Data;
import lombok.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day09_MarbleMania implements Executable {
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
        // Initialize the circular queue
        CircularQueue<Integer> game = new CircularQueue<>();
        // Keep the leaderboard of players
        // Had to use a Long for part 2, but there was no StackOverflow so it's weird only clue
        // was that it would return a negative value
        Map<Integer, Long> leaderboard = new HashMap<>();

        // Solution from reddit, because mine was too convoluted and was too slow due to the use
        // of the wrong datastructure. This one is concise and with the help of the homemade
        // circular queue is much better than the old one.
        // https://www.reddit.com/r/adventofcode/comments/a4i97s/2018_day_9_solutions/ebepyc7

        // We add one to the number of iterations because else one marble will be missing from
        // the game. The marbles number is: 71852. So by doing a
        // for loop like this (with multiplicator equal to 1):
        // for (int i = 0; i < (marbles * multiplicator); i++)
        // We would go through marble: 0 to 71851. We would have 71852 marbles but that's not
        // what we want. We want to go through marble: 0 to 71852.
        // So we can either add one to the number of iterations or use a less or equal operator.
        // The two should be valid.
        for (int i = 0; i < (marbles * multiplicator) + 1; i++) {
            // If i is not zero and a multiple of 23 then do special rule
            if (i != 0 && i % 23 == 0) {
                // Rotate the queue by 7 anti clock wise
                game.rotate(7);
                // Add the score to the leaderboard for the player
                // we use merge so if there is no mapping already, it creates one
                // and put the value, else it just merge the values
                leaderboard.merge(
                    i % players,
                    i + new Long(game.pop()),
                    Long::sum
                );
                // Rotate the queue back to put it in the desired state
                game.rotate(-1);
            } else {
                // Rotate the queue to insert next element
                // By rotating the queue, we can effectively just insert items at the end of the
                // queue instead of trying to figure out where we should insert them in the queue
                // it's more effective.
                game.rotate(-1);
                // Add the element to the end of the queue
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

    // Implemented my own CircularQueue (this may not be the correct name) because
    // it appears in Java this doesn't exists. The queue only handle push, pop and rotate.
    // This is a true double linked list, each node has no other info than the previous node,
    // the next node, and the value it holds. Thus making it faster than the Java implementation
    // LinkedList.
    private class CircularQueue<T> {
        private QueueNode head;
        private QueueNode tail;

        @Data
        private class QueueNode {
            private QueueNode head;
            private QueueNode tail;
            private T value;

            QueueNode(T value) {
                // For a new node initialize the head and tail to null
                this.head = null;
                this.tail = null;
                this.value = value;
            }

            void link(QueueNode newNode) {
                // The new node's head is the current node
                newNode.setHead(this);
                // The new node's tail is the tail of the current node
                newNode.setTail(this.tail);

                // The head of the tail of the current node is the new node
                this.tail.setHead(newNode);
                // The tail of the current node is now the new node
                this.tail = newNode;
            }

            T unlink() {
                // The tail of the current node's head is the tail of the current node
                this.head.setTail(this.tail);
                // The head of the current node's tail is the head of the current node
                this.tail.setHead(this.head);

                this.head = null;
                this.tail = null;

                return this.value;
            }
        }

        void push(T value) {
            // Create a node with the given value
            QueueNode node = new QueueNode(value);

            // If the queue has just been created
            if (head == null && tail == null) {
                // The head and the tail are the same as there is only one element
                head = node;
                tail = node;

                // As there is only one node for the moment
                // the node is linked back to itself, the head's tail is the tail
                // and the tail's head is the head, but the head and the tail are the same
                head.setTail(tail);
                tail.setHead(head);
            } else if (head != null && tail != null) {
                // Link the tail and the new node
                tail.link(node);
                // Set the tail to the new node
                tail = node;
            }
        }

        T pop() {
            // Store the node which will be removed
            QueueNode toBeRemoved = tail;
            // Set the new tail to be the head of the current tail
            tail = toBeRemoved.getHead();
            // Unlink the old tail
            return toBeRemoved.unlink();
        }

        /**
         * Rotate the queue with the degree given.
         * @param degrees If degrees is greater than 0 then it will rotate anti-clockwise,
         *                if degrees is less than 0 then it will rotate clockwise
         */
        void rotate(int degrees) {
            // If the list is empty then do nothing
            if (head == null && tail == null) {
                return;
            }
            if (degrees > 0) {
                // To rotate anti-clockwise we get the head of the head and tail
                // if you would represent the circle as a line, it would go to the left
                for (int i = 0; i < degrees; i++) {
                    head = head.getHead();
                    tail = tail.getHead();
                }
            } else if (degrees < 0) {
                // To rotate clockwise we get the tail of the head and tail
                // if you would represent the circle as a line, it would go to the right
                for (int i = 0; i < Math.abs(degrees); i++) {
                    head = head.getTail();
                    tail = tail.getTail();
                }
            }
        }

        @Override
        public String toString() {
            // Create a StringBuilder instace
            StringBuilder stringBuilder = new StringBuilder();
            // Append the name of the object for easier reading
            stringBuilder.append("CircularQueue: ");
            // Create a list which will contains all the values of the nodes as strings
            List<String> nodes = new ArrayList<>();

            // Initialize the beginning node as the head
            QueueNode currentNode = head;

            do {
                // Add the node's value to the list
                nodes.add(currentNode.getValue().toString());
                // The next node is the tail of the current node
                currentNode = currentNode.getTail();
            // Do a full circle, when currentNode is equal to the head it means
            // we got back to the beginning
            } while (currentNode != head);

            // Append all the values to the stringBuilder using String.join to use a delimiter
            // between them
            stringBuilder.append(String.join(" -> ", nodes));

            // Return the string representation of the stringBuilder
            return stringBuilder.toString();
        }
    }
}
