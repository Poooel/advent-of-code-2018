package days;

import com.rits.cloning.Cloner;
import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Day15_BeverageBandits implements Executable {
    @Override
    public String executePartOne() {
        List<String> input = ChallengeHelper.readInputData(15);
        GameState initialGameState = parseInitialGameState(input);
        GameState endGameState = proceedToCombat(initialGameState);
        return Integer.toString(sumOfHitPoints(endGameState.getCharacters()) * endGameState.getRounds());
    }

    @Override
    public String executePartTwo() {
        List<String> input = ChallengeHelper.readInputData(15);
        GameState initialGameState = parseInitialGameState(input);
        GameState endGameState = findProfitableOutcomeForElves(initialGameState);
        return Integer.toString(sumOfHitPoints(endGameState.getCharacters()) * endGameState.getRounds());
    }

    private GameState parseInitialGameState(List<String> input) {
        // Initialize a new Cave with height being the size of the input list
        // and the width the lenght of each string in the input list
        // as each lines is the same lenght we can take the first one for reference
        Cave cave = new Cave(input.size(), input.get(0).length());
        List<Character> units = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            // Convert the string to a char array
            char[] line = input.get(i).toCharArray();

            for (int j = 0; j < line.length; j++) {
                // Extract char corresponding to the underlying terrain
                char terrain = line[j];
                Coordinates coordinates = new Coordinates(j, i);

                switch (terrain) {
                    case '#':
                        cave.setTerrain(coordinates, TerrainType.WALL);
                        break;
                    case '.':
                        cave.setTerrain(coordinates, TerrainType.OPEN_CAVERN);
                        break;
                    case 'G':
                        // If there is a unit, the terrain under the unit is an open cavern
                        cave.setTerrain(coordinates, TerrainType.OPEN_CAVERN);
                        units.add(new Character(CharacterType.GOBLIN, coordinates));
                        break;
                    case 'E':
                        cave.setTerrain(coordinates, TerrainType.OPEN_CAVERN);
                        units.add(new Character(CharacterType.ELF, coordinates));
                        break;
                }
            }
        }

        // Return the initial game state containing the cave, the units and the initial number of rounds
        return new GameState(cave, units, 0);
    }

    private GameState proceedToCombat(GameState gameState) {
        // Extract the list of units for easier access
        List<Character> units = gameState.getCharacters();

        // Infinite loop as we don't know when the combat will be finished
        while (true) {
            // Sort the units in the reading for order to ensure that each unit
            // play at the right time
            units = sortUnits(units);

            for (Character unit : units) {
                // Skip the dead units
                if (!unit.isDead()) {
                    // Conditions to know if the combat is finished
                    // If no target is detected, the combat is done
                    if (isThereTargets(units, unit)) {
                        // If the unit is in range of a target
                        // attack without moving
                        if (isInRangeOfAnotherUnit(units, unit)) {
                            attack(units, unit);
                        } else {
                            // If the unit is not in range, move then try to attack
                            move(gameState, unit);
                            attack(units, unit);
                        }
                    } else {
                        // No more targets, combat ends
                        return gameState;
                    }
                }

            }

            // Increment the number of rounds
            gameState.setRounds(gameState.getRounds() + 1);
        }
    }

    private GameState findProfitableOutcomeForElves(GameState gameState) {
        // We can't lose any elves for this part
        int acceptableElvesLosses = 0;
        // The initial attack power for the elves
        int currentAttackPower = 3;
        // The number of elves present in the initial game state
        int initialNumberOfElves = (int) countElves(gameState.getCharacters());
        // Creating a cloner instance to avoid creating it at every turn of the loop
        // we need this because we need to make a deep copy of game state, because
        // we don't want the combat to alter it because we will reuse it
        Cloner cloner = new Cloner();

        while (true) {
            // Do the combat with a deep clone of the game state to avoid modification
            // on the original object
            GameState currentGameState = proceedToCombat(cloner.deepClone(gameState));
            // Count the elves left after the combat
            int numberOfElves = (int) countElves(currentGameState.getCharacters());

            // If there is more than 0 elves that died during the combat
            // increase the attack power
            if (initialNumberOfElves - numberOfElves > acceptableElvesLosses) {
                changeAttackPowerOfElves(gameState, currentAttackPower + 1);
                currentAttackPower += 1;
            // If no elves have been lost, we found the solution
            } else if (initialNumberOfElves - numberOfElves == acceptableElvesLosses) {
                return currentGameState;
            }
        }
    }

    private long countElves(List<Character> characters) {
        return characters
            .stream()
            // Only keep the unit who are elves
            .filter(unit -> unit.getCharacterType() == CharacterType.ELF)
            // Don't count dead elves
            .filter(unit -> !unit.isDead())
            .count();
    }

    private void changeAttackPowerOfElves(GameState gameState, int attackPower) {
        for (Character unit : gameState.getCharacters()) {
            // Change the attack power of only elves not the goblins
            if (unit.getCharacterType() == CharacterType.ELF) {
                unit.setAttackPower(attackPower);
            }
        }

    }

    private int sumOfHitPoints(List<Character> units) {
        return units
            .stream()
            .filter(unit -> !unit.isDead())
            .mapToInt(Character::getHitPoints)
            .sum();
    }

    private void attack(List<Character> units, Character unit) {
        List<Character> targets = getTargetsInRange(units, unit);

        // If there is no targets in range, finish the turn of the unit
        if (targets.size() > 0) {
            Character target = chooseTargetsWithFewestHitPoints(targets);
            unit.attack(target);
        }
    }

    private void move(GameState gameState, Character unit) {
        // 1. Identify open squares
        List<Coordinates> openSquares = identifyOpenSquares(gameState, unit);

        // 2. Compute paths to the destination
        List<Path> paths = findPaths(gameState, unit, openSquares);

        // If there is no path to the destination, finish turn
        if (paths.size() == 0) {
            return;
        }

        paths = getShortestPaths(paths);

        // 3. If there is multiple equally shortest path to different destinations, choose destination in reading order
        List<Coordinates> destinations = paths.stream().map(Path::getDestination).collect(Collectors.toList());

        Coordinates destination = sortCoordinates(destinations).get(0);

        // 4. Compute all the shortest path to destination
        paths = paths.stream().filter(path -> path.getDestination() == destination).collect(Collectors.toList());

        // 5. If there is multiple equally shortest path to destination, choose first step in reading order
        List<Coordinates> firstSteps = paths.stream().map(path -> path.getPath().get(0)).collect(Collectors.toList());

        Coordinates firstStep = sortCoordinates(firstSteps).get(0);

        // 6. Move
        unit.moveTo(firstStep);
    }

    private List<Path> findPaths(GameState gameState, Character unit, List<Coordinates> openSquares) {
        List<Path> paths = new ArrayList<>();

        // For each open square evaluate the distance between the
        for (Coordinates openSquare : openSquares) {
            // adjacent coordinates and the open square
            for (Coordinates adjacentCoordinate : unit.getCoordinates().getAdjacentCoordinates()) {
                // check if the adjacent coordinates is an open square (open cavern, no unit, ...)
                if (isOpenSquare(adjacentCoordinate, gameState)) {
                    // Compute the shortest path
                    List<Coordinates> shortestPath = findShortestPath(adjacentCoordinate, openSquare, gameState);

                    // If there is no path, do not add it
                    if (shortestPath != null) {
                        paths.add(
                            new Path(
                                adjacentCoordinate,
                                openSquare,
                                shortestPath
                            )
                        );
                    }
                }
            }
        }

        return paths;
    }

    private List<Path> getShortestPaths(List<Path> paths) {
        List<Path> shortestPaths = new ArrayList<>();
        int shortestDistance = Integer.MAX_VALUE;

        // Find the list of shortest path in the list of paths
        for (Path path : paths) {
            if (path.getPath().size() < shortestDistance) {
                shortestDistance = path.getPath().size();
                shortestPaths.clear();
                shortestPaths.add(path);
            } else if (path.getPath().size() == shortestDistance) {
                shortestPaths.add(path);
            }
        }

        return shortestPaths;
    }

    private Character chooseTargetsWithFewestHitPoints(List<Character> units) {
        List<Character> targetsWithFewestHitPoints = new ArrayList<>();
        int fewestHitPoints = Integer.MAX_VALUE;

        // Find the targets with the fewest hit points in the target list
        for (Character unit : units) {
            if (unit.getHitPoints() < fewestHitPoints) {
                fewestHitPoints = unit.getHitPoints();
                targetsWithFewestHitPoints.clear();
                targetsWithFewestHitPoints.add(unit);
            } else if (unit.getHitPoints() == fewestHitPoints) {
                targetsWithFewestHitPoints.add(unit);
            }
        }

        // Sort the units in reading order
        targetsWithFewestHitPoints = sortUnits(targetsWithFewestHitPoints);

        // Only pick the first one
        return targetsWithFewestHitPoints.get(0);
    }

    private List<Character> sortUnits(List<Character> units) {
        // Sort unit in reading order: top-to-bottom and left-to-right
        Comparator<Character> comparator = Comparator.comparing(unit -> unit.getCoordinates().getY());
        comparator = comparator.thenComparing(character -> character.getCoordinates().getX());

        return units
            .stream()
            .sorted(comparator)
            .collect(Collectors.toList());
    }

    private List<Coordinates> sortCoordinates(List<Coordinates> coordinates) {
        // Do the same for coordinates
        Comparator<Coordinates> comparator = Comparator.comparing(Coordinates::getY);
        comparator = comparator.thenComparing(Coordinates::getX);

        return coordinates
            .stream()
            .sorted(comparator)
            .collect(Collectors.toList());
    }

    private List<Character> identifyPossibleTargets(List<Character> units, Character currentUnit) {
        return units
            // Identify possible targets that are
            .stream()
            // Of opposed character type
            .filter(unit -> unit.getCharacterType() != currentUnit.getCharacterType())
            // Not dead
            .filter(unit -> !unit.isDead())
            .collect(Collectors.toList());
    }

    private boolean isThereTargets(List<Character> units, Character currentUnit) {
        // There is targets if there is at least 1 possible target
        return identifyPossibleTargets(units, currentUnit).size() != 0;
    }

    private List<Coordinates> identifyOpenSquares(GameState gameState, Character currentUnit) {
        // 1. Identify possible targets
        List<Character> possibleTargets = identifyPossibleTargets(gameState.getCharacters(), currentUnit);
        Set<Coordinates> openSquares = new HashSet<>();

        // 2. For each possible target
        for (Character possibleTarget : possibleTargets) {
            // 3. For each direction (up, down, left, right)
            for (Coordinates coordinates : possibleTarget.getCoordinates().getAdjacentCoordinates()) {
                // 4. Is it an open square ?
                if (isOpenSquare(coordinates, gameState)) {
                    // 5. Add open square to set
                    openSquares.add(coordinates);
                }
            }
        }

        // 6. Return set
        return new ArrayList<>(openSquares);
    }
    
    private boolean isOpenSquare(Coordinates coordinates, GameState gameState) {
        // 1. Is it different from terrain OPEN_CAVERN ?
        if (gameState.getCave().getTerrain(coordinates) != TerrainType.OPEN_CAVERN) {
            return false;
        } else {
            // 2. Is there an unit on these coordinates ?
            for (Character unit : gameState.getCharacters()) {
                if (unit.isDead()) {
                    continue;
                }
                if (coordinates.equals(unit.getCoordinates())) {
                    return false;
                }
            }
        }

        // 3. If answer to both of these question is no then it is an open square
        return true;
    }

    private boolean isInRangeOfAnotherUnit(List<Character> units, Character currentUnit) {
        return getTargetsInRange(units, currentUnit).size() != 0;
    }

    private List<Character> getTargetsInRange(List<Character> units, Character currentUnit) {
        // 1. Identify possible targets
        List<Character> possibleTargets = identifyPossibleTargets(units, currentUnit);
        List<Coordinates> unitAdjacentCoordinates = currentUnit.getCoordinates().getAdjacentCoordinates();
        Set<Character> targetsInRange = new HashSet<>();

        // 2. For each possible target
        for (Character possibleTarget : possibleTargets) {
            // 3. Is it in range ?
            if (unitAdjacentCoordinates.contains(possibleTarget.getCoordinates())) {
                // 4. Add it to set of targets in range
                targetsInRange.add(possibleTarget);
            }
        }

        // 5. Return set
        return new ArrayList<>(targetsInRange);
    }

    /**
     * https://en.wikipedia.org/wiki/A*_search_algorithm#Pseudocode
     */
    private List<Coordinates> findShortestPath(Coordinates source, Coordinates destination, GameState gameState) {
        Set<Coordinates> closedSet = new HashSet<>();
        Set<Coordinates> openSet = new HashSet<>();
        Map<Coordinates, Coordinates> cameFrom = new HashMap<>();
        Map<Coordinates, Integer> gScore = new HashMap<>();
        Map<Coordinates, Integer> fScore = new HashMap<>();

        openSet.add(source);
        gScore.put(source, 0);
        fScore.put(source, source.distanceTo(destination));

        while (!openSet.isEmpty()) {
            Coordinates current = getLowestCoordinatesScore(fScore, openSet);

            if (current.equals(destination)) {
                List<Coordinates> path = new ArrayList<>();
                path.add(current);

                while (cameFrom.keySet().contains(current)) {
                    current = cameFrom.get(current);
                    path.add(0, current);
                }

                return path;
            }

            openSet.remove(current);
            closedSet.add(current);

            List<Coordinates> neighbors = current.getAdjacentCoordinates();

            for (Coordinates neighbor : neighbors) {
                if (isOpenSquare(neighbor, gameState)) {
                    if (closedSet.contains(neighbor)) {
                        continue;
                    }

                    int tentativeGScore = gScore.get(current) + current.distanceTo(neighbor);

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    } else if (tentativeGScore >= gScore.get(neighbor)) {
                        continue;
                    }

                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, gScore.get(neighbor) + neighbor.distanceTo(destination));
                }
            }
        }

        return null;
    }

    private Coordinates getLowestCoordinatesScore(Map<Coordinates, Integer> fScore, Set<Coordinates> openSet) {
        int min = Integer.MAX_VALUE;
        Coordinates minCoordinates = null;

        // Find the coordinates that is in the openSet and in the fScore map with the lowest score
        for (Coordinates coordinates : openSet) {
            if (fScore.get(coordinates) < min) {
                min = fScore.get(coordinates);
                minCoordinates = coordinates;
            }
        }

        return minCoordinates;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private final class GameState {
        private Cave cave;
        private List<Character> characters;
        private int rounds;
    }

    @Data
    private final class Character {
        private final CharacterType characterType;
        private Coordinates coordinates;
        private int hitPoints = 200;
        private int attackPower = 3;
        private boolean dead;

        Character(CharacterType characterType, Coordinates coordinates) {
            this.characterType = characterType;
            this.coordinates = coordinates;
        }

        void attack(Character target) {
            target.setHitPoints(target.getHitPoints() - this.getAttackPower());

            if (target.getHitPoints() <= 0) {
                target.setDead(true);
            }
        }

        void moveTo(Coordinates coordinates) {
            this.coordinates = coordinates;
        }
    }

    @Data
    @AllArgsConstructor
    private final class Coordinates {
        private int x;
        private int y;

        int distanceTo(Coordinates coordinates) {
            return (int) Math.sqrt(
                Math.pow(coordinates.getX() - this.getX(), 2)
                    +
                Math.pow(coordinates.getY() - this.getY(), 2)
            );
        }

        Coordinates up() {
            return new Coordinates(this.getX(), this.getY() - 1);
        }

        Coordinates down() {
            return new Coordinates(this.getX(), this.getY() + 1);
        }

        Coordinates left() {
            return new Coordinates(this.getX() + 1, this.getY());
        }

        Coordinates right() {
            return new Coordinates(this.getX() - 1, this.getY());
        }

        List<Coordinates> getAdjacentCoordinates() {
            return Arrays.asList(
                this.up(),
                this.down(),
                this.left(),
                this.right()
            );
        }
    }

    @Getter
    private final class Cave {
        private final TerrainType[][] rawCave;

        Cave(int height, int width) {
            rawCave = new TerrainType[height][];

            for (int i = 0; i < height; i++) {
                rawCave[i] = new TerrainType[width];
            }
        }

        private TerrainType getTerrain(int x, int y) {
            return rawCave[y][x];
        }

        TerrainType getTerrain(Coordinates coordinates) {
            return getTerrain(coordinates.getX(), coordinates.getY());
        }

        private void setTerrain(int x, int y, TerrainType terrainType) {
            rawCave[y][x] = terrainType;
        }

        void setTerrain(Coordinates coordinates, TerrainType terrainType) {
            setTerrain(coordinates.getX(), coordinates.getY(), terrainType);
        }
    }

    @Value
    private final class Path {
        private final Coordinates source;
        private final Coordinates destination;
        private final List<Coordinates> path;
    }

    private enum TerrainType {
        WALL,
        OPEN_CAVERN
    }

    private enum CharacterType {
        ELF,
        GOBLIN
    }
}
