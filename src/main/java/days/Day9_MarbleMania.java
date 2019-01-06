package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Day9_MarbleMania implements Executable {
    private int marbleCounter = 1;

    @Override
    public String executePartOne() {
        List<String> inputs = ChallengeHelper.readInputData(9);
        MarbleInitialParameters marbleInitialParameters = parseInput(inputs);
        MarbleGameContext marbleGameContext = playMarbleGame(marbleInitialParameters);
        Player bestPlayer = findPlayerWithHighestScore(marbleGameContext.getPlayers());
        return String.valueOf(bestPlayer.getScore());
    }

    @Override
    public String executePartTwo() {
        return null;
    }

    private Player findPlayerWithHighestScore(List<Player> players) {
        return players
            .stream()
            .max(Comparator.comparingInt(Player::getScore))
            .get();
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

    private MarbleGameContext playMarbleGame(MarbleInitialParameters marbleInitialParameters) {
        LinkedList<Marble> game = new LinkedList<>();
        List<Player> players = initializePlayerPool(marbleInitialParameters.getNumberOfPlayers());
        PlayerPointer playerPointer = new PlayerPointer(players);
        Marble currentMarble = new Marble(0, 0);

        // Place first two marbles
        // Place first marble
        game.offer(currentMarble);
        // Place second marble
        currentMarble = new Marble(1, 1);
        game.offer(currentMarble);
        playerPointer.nextPlayer();

        // Start at 2 because we already placed 2 marbles
        for (int i = 1; i < marbleInitialParameters.getNumberOfMarbles(); i++) {
            marbleCounter++;
            currentMarble = placeMarble(currentMarble, playerPointer.getCurrentPlayer(), game);
            playerPointer.nextPlayer();
        }

        return new MarbleGameContext(game, players);
    }

    private Marble placeMarble(
        Marble currentMarble,
        Player currentPlayer,
        LinkedList<Marble> game
    ) {
        Marble marbleToPlace = computeNextMarble(currentMarble, game);

        if (marbleToPlace.getNumber() % 23 == 0) {
            currentPlayer.addToScore(marbleToPlace.getNumber());
            Marble toRemoveMarble = getMarble7MarblesCounterClockWise(currentMarble, game);
            currentPlayer.addToScore(toRemoveMarble.getNumber());
            Marble newCurrentMarble = getAdjacentClockWiseMarble(toRemoveMarble, game);
            game.remove(toRemoveMarble.getIndex());
            return newCurrentMarble;
        } else {
            game.add(marbleToPlace.getIndex(), marbleToPlace);
            return marbleToPlace;
        }
    }

    private Marble computeNextMarble(
        Marble currentMarble,
        LinkedList<Marble> game
    ) {
        int currentMarbleIndex = game.indexOf(currentMarble);
        int gameSize = game.size();
        int pointer = currentMarbleIndex;

        for (int i = 0; i < 2; i++) {
            pointer++;
            if (pointer == gameSize + 1) {
                pointer = 1;
            }
        }

        return new Marble(pointer, marbleCounter);
    }

    private Marble getMarble7MarblesCounterClockWise(
        Marble currentMarble,
        LinkedList<Marble> game
    ) {
        int currentMarbleIndex = game.indexOf(currentMarble);
        int gameSize = game.size();
        int pointer = currentMarbleIndex;

        for (int i = 0; i < 7; i++) {
            pointer--;
            if (pointer == -1) {
                pointer = gameSize - 1;
            }
        }

        return new Marble(pointer, game.get(pointer).getNumber());
    }

    private Marble getAdjacentClockWiseMarble(
        Marble currentMarble,
        LinkedList<Marble> game
    ) {
        int currentMarbleIndex = 0;

        for (int i = 0; i < game.size(); i++) {
            if (game.get(i).getNumber() == currentMarble.getNumber()) {
                currentMarbleIndex = i;
                break;
            }
        }

        int gameSize = game.size();
        int pointer = currentMarbleIndex;

        pointer++;
        if (pointer == gameSize + 1) {
            pointer = 0;
        }

        return game.get(pointer);
    }

    private List<Player> initializePlayerPool(int numberOfPlayers) {
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < numberOfPlayers; i++) {
            players.add(
                new Player(i, 0)
            );
        }

        return players;
    }

    @Value
    private class MarbleInitialParameters {
        private int numberOfMarbles;
        private int numberOfPlayers;
    }

    @Value
    private class MarbleGameContext {
        private LinkedList<Marble> game;
        private List<Player> players;
    }

    @Data
    @AllArgsConstructor
    private class Player {
        private int number;
        private int score;

        void addToScore(int toAdd) {
            score += toAdd;
        }
    }

    @Data
    private class PlayerPointer {
        private List<Player> players;
        private Player currentPlayer;
        private int playerIndex;

        PlayerPointer(List<Player> players) {
            this.players = players;
            this.playerIndex = 0;
            this.currentPlayer = players.get(playerIndex);
        }

        void nextPlayer() {
            playerIndex++;

            if (playerIndex >= players.size()) {
                playerIndex = 0;
            }

            currentPlayer = players.get(playerIndex);
        }
    }

    @Value
    private class Marble {
        private int index;
        private int number;
    }
}
