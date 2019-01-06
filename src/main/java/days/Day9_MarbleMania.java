package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
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

    private int playMarbleGame(int marbles, int players, int multiplicator) {
        List<Integer> game = new ArrayList<>(marbles * multiplicator);
        Map<Integer, Integer> leaderboard = new HashMap<>();

        // For debugging
        double percentage = 0;

        for (int i = 0; i < (marbles * multiplicator) + 1; i++) {
            // --- For debugging ---
            float currentPercentage = i * 100f / (marbles * multiplicator);

            if (Math.round(currentPercentage) != percentage) {
                System.out.println(percentage + "% done.");
                percentage = Math.round(currentPercentage);
            }
            // --- For debugging ---

            if (i != 0 && i % 23 == 0) {
                Collections.rotate(game, 7);
                leaderboard.merge(
                    i % players,
                    i + game.remove(game.size() - 1),
                    Integer::sum
                );
                Collections.rotate(game, -1);
            } else {
                Collections.rotate(game, -1);
                game.add(i);
            }
        }

        return leaderboard.values().stream().mapToInt(Integer::intValue).max().getAsInt();
    }

    @Value
    private class MarbleInitialParameters {
        private int numberOfMarbles;
        private int numberOfPlayers;
    }
}
