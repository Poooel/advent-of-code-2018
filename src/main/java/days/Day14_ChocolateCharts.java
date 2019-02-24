package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Day14_ChocolateCharts implements Executable {
    @Override
    public String executePartOne() {
        List<String> input = ChallengeHelper.readInputData(14);
        // Parse the integer and add 10 because we want the 10 next recipes after the numberOfRecipes
        int numberOfRecipes = parseInput(input) + 10;
        return convertListToString(findBestRecipes(numberOfRecipes));
    }

    @Override
    public String executePartTwo() {
        List<String> input = ChallengeHelper.readInputData(14);
        // Parse the integer and do not add 10 because we don't need this here
        int numberOfRecipes = parseInput(input);
        return String.format("%d", findRecipesToLeft(numberOfRecipes));
    }

    private int parseInput(List<String> input) {
        // Get only the first string from the input because we know the
        // input file is composed of only one integer on the first line
        return Integer.parseInt(input.get(0));
    }

    private GameState nextGameState(GameState gameState) {
        // Compute the new recipe
        int newRecipe = gameState.getRecipes().get(gameState.getElf1())
            + gameState.getRecipes().get(gameState.getElf2());

        // If the new recipe is equal or greater than 10
        // add to the recipes the two digits of the number
        // to get the first one we divide by 10 (14/10 = 1)
        // to get the second one we do the modulo by 10 (14%10 = 4)
        if (newRecipe >= 10) {
            gameState.getRecipes().add(newRecipe / 10);
            gameState.getRecipes().add(newRecipe % 10);
        // If the new recipe is less than 10 then add it to the recipes
        } else {
            gameState.getRecipes().add(newRecipe);
        }

        // Move elf1 to the its next current recipe
        gameState.setElf1(
            gameState.getElf1()
                // Using modulo here to avoid handling a loop
                + (1 + gameState.getRecipes().get(gameState.getElf1())) % gameState.getRecipes().size()
        );

        // If the elf1 has an index greater than the size of the recipes
        if (gameState.getElf1() >= gameState.getRecipes().size()) {
            // Process it so it start from the beginning
            gameState.setElf1(gameState.getElf1() - gameState.getRecipes().size());
        }

        // Do the same for elf2
        gameState.setElf2(
            gameState.getElf2()
                + (1 + gameState.getRecipes().get(gameState.getElf2())) % gameState.getRecipes().size()
        );

        if (gameState.getElf2() >= gameState.getRecipes().size()) {
            gameState.setElf2(gameState.getElf2() - gameState.getRecipes().size());
        }

        // Return updated game state
        return gameState;
    }

    private GameState initializeGameState(int arraySize) {
        // The recipes start with 3 and 7
        // Initiliaze the list with a given size to speed up the processing
        List<Integer> recipes = new ArrayList<>(arraySize);
        recipes.add(3);
        recipes.add(7);
        // First elf point to the first recipe (3)
        int elf1 = 0;
        // Second elf point to the second recipe (7)
        int elf2 = 1;

        return new GameState(recipes, elf1, elf2);
    }
    
    private List<Integer> findBestRecipes(int numberOfRecipes) {
        // Initialize the game state with a size of the number of recipes
        // because we know it can't get over this number
        GameState gameState = initializeGameState(numberOfRecipes);

        // Compute all the game states
        while (gameState.recipes.size() <= numberOfRecipes) {
            gameState = nextGameState(gameState);
        }

        // Return the last 10 recipes after the numberOfRecipes
        return gameState.recipes.subList(numberOfRecipes - 10, numberOfRecipes);
    }

    private int findRecipesToLeft(int pattern) {
        // From testing (and having the answer), it is way faster to calculate a sample
        // and search through it instead of searching every new game state. 25 000 000 is a good
        // compromise between search and safety (to be sure to find the answer in our case)
        int sampleSize = 25_000_000;
        // Initilize the game state with the sample size
        GameState gameState = initializeGameState(sampleSize);

        // Compute all the game states
        for (int i = 0; i < sampleSize; i++) {
            gameState = nextGameState(gameState);
        }

        // Find the pattern into this big list and return the index of it
        return Collections.indexOfSubList(gameState.getRecipes(), convertIntToListOfDigits(pattern));
    }

    /**
     * Convert a list of ints to a string, with all the numbers joined together without a separator.
     *
     * @param ints The list of ints to transform
     * @return A string containing all the ints from the list joined together without a separator
     */
    private String convertListToString(List<Integer> ints) {
        List<String> resultAsString = ints.stream().map(String::valueOf).collect(Collectors.toList());
        return String.join("", resultAsString);
    }

    /**
     * Convert an int to a list of its digits.
     * https://stackoverflow.com/a/8033593/7621349
     *
     * @param integer The int to convert to a list
     * @return The list of digits of the int
     */
    private List<Integer> convertIntToListOfDigits(int integer) {
        String temp = Integer.toString(integer);
        Integer[] newGuess = new Integer[temp.length()];
        for (int i = 0; i < temp.length(); i++)
        {
            newGuess[i] = temp.charAt(i) - '0';
        }

        return Arrays.asList(newGuess);
    }

    @Data
    @AllArgsConstructor
    private final class GameState {
        List<Integer> recipes;
        int elf1;
        int elf2;
    }
}
