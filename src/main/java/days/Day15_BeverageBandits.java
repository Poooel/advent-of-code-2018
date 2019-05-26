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
import java.util.Collections;
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
        Cave cave = new Cave(input.size(), input.get(0).length());
        List<Character> characters = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            char[] line = input.get(i).toCharArray();

            for (int j = 0; j < line.length; j++) {
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
                        cave.setTerrain(coordinates, TerrainType.OPEN_CAVERN);
                        characters.add(new Character(CharacterType.GOBLIN, coordinates));
                        break;
                    case 'E':
                        cave.setTerrain(coordinates, TerrainType.OPEN_CAVERN);
                        characters.add(new Character(CharacterType.ELF, coordinates));
                        break;
                }
            }
        }

        return new GameState(cave, characters, 0);
    }

    private GameState proceedToCombat(GameState gameState) {
        List<Character> units = gameState.getCharacters();

        while (true) {
            units = sortUnits(units);

            for (Character unit : units) {
                if (!unit.isDead()) {
                    if (isThereTargets(units, unit)) {
                        if (isInRangeOfAnotherUnit(units, unit)) {
                            // attack
                            attack(units, unit);
                        } else {
                            // move
                            move(gameState, unit);
                            attack(units, unit);
                        }
                    } else {
                        // no more targets combat ends
                        return gameState;
                    }

                    debug(gameState);
                }

            }

            gameState.setRounds(gameState.getRounds() + 1);
        }
    }

    private GameState findProfitableOutcomeForElves(GameState gameState) {
        int acceptableElvesLosses = 0;
        int currentAttackPower = 3;
        int initialNumberOfElves = (int) countElves(gameState.getCharacters());
        int numberOfElves;
        Cloner cloner = new Cloner();

        while (true) {
            GameState currentGameState = proceedToCombat(cloner.deepClone(gameState));
            numberOfElves = (int) countElves(currentGameState.getCharacters());

            if (initialNumberOfElves - numberOfElves > acceptableElvesLosses) {
                changeAttackPowerOfElves(gameState, currentAttackPower + 1);
                currentAttackPower += 1;
            } else if (initialNumberOfElves - numberOfElves == acceptableElvesLosses) {
                return currentGameState;
            }
        }
    }

    private long countElves(List<Character> characters) {
        return characters
            .stream()
            .filter(unit -> unit.getCharacterType() == CharacterType.ELF)
            .filter(unit -> !unit.isDead())
            .count();
    }

    private void changeAttackPowerOfElves(GameState gameState, int attackPower) {
        for (Character unit : gameState.getCharacters()) {
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

        if (targets.size() > 0) {
            Character target = chooseTargetsWithFewestHitPoints(targets);
            unit.attack(target);
        }
    }

    private void move(GameState gameState, Character unit) {
        // 1. Identify open squares
        // 2. Compute paths to the destination
        // 3. If there is multiple equally shortest path to different destinations, choose destination in reading order
        // 4. Compute all the shortest path to destination
        // 5. If there is multiple equally shortest path to destination, choose first step in reading order
        // 6. Move

        // 1.
        List<Coordinates> openSquares = identifyOpenSquares(gameState, unit);

        // 2.
        List<Path> paths = findPaths(gameState, unit, openSquares);

        if (paths.size() == 0) {
            return;
        }

        paths = getShortestPaths(paths);

        // 3.
        List<Coordinates> destinations = paths.stream().map(Path::getDestination).collect(Collectors.toList());

        Coordinates destination = sortCoordinates(destinations).get(0);

        // 4.
        paths = paths.stream().filter(path -> path.getDestination() == destination).collect(Collectors.toList());

        // 5.
        List<Coordinates> firstSteps = paths.stream().map(path -> path.getPath().get(0)).collect(Collectors.toList());

        Coordinates firstStep = sortCoordinates(firstSteps).get(0);

        // 6.
        unit.moveTo(firstStep);
    }

    private List<Path> findPaths(GameState gameState, Character unit, List<Coordinates> openSquares) {
        List<Path> paths = new ArrayList<>();

        for (Coordinates openSquare : openSquares) {
            for (Coordinates adjacentCoordinate : unit.getCoordinates().getAdjacentCoordinates()) {
                if (isOpenSquare(adjacentCoordinate, gameState)) {
                    List<Coordinates> shortestPath = findShortestPath(adjacentCoordinate, openSquare, gameState);

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

        for (Character unit : units) {
            if (unit.getHitPoints() < fewestHitPoints) {
                fewestHitPoints = unit.getHitPoints();
                targetsWithFewestHitPoints.clear();
                targetsWithFewestHitPoints.add(unit);
            } else if (unit.getHitPoints() == fewestHitPoints) {
                targetsWithFewestHitPoints.add(unit);
            }
        }

        targetsWithFewestHitPoints = sortUnits(targetsWithFewestHitPoints);

        return targetsWithFewestHitPoints.get(0);
    }

    private List<Character> sortUnits(List<Character> units) {
        Comparator<Character> comparator = Comparator.comparing(unit -> unit.getCoordinates().getY());
        comparator = comparator.thenComparing(character -> character.getCoordinates().getX());

        return units
            .stream()
            .sorted(comparator)
            .collect(Collectors.toList());
    }

    private List<Coordinates> sortCoordinates(List<Coordinates> coordinates) {
        Comparator<Coordinates> comparator = Comparator.comparing(Coordinates::getY);
        comparator = comparator.thenComparing(Coordinates::getX);

        return coordinates
            .stream()
            .sorted(comparator)
            .collect(Collectors.toList());
    }

    private List<Character> identifyPossibleTargets(List<Character> units, Character currentUnit) {
        return units
            .stream()
            .filter(unit -> unit.getCharacterType() != currentUnit.getCharacterType())
            .filter(unit -> !unit.isDead())
            .collect(Collectors.toList());
    }

    private boolean isThereTargets(List<Character> units, Character currentUnit) {
        return identifyPossibleTargets(units, currentUnit).size() != 0;
    }

    private List<Coordinates> identifyOpenSquares(GameState gameState, Character currentUnit) {
        // 1. Identify possible targets
        // 2. For each possible target
        // 3. For each direction (up, down, left, right)
        // 4. Is it an open square ?
        // 5. Add open square to set
        // 6. Return set

        // 1.
        List<Character> possibleTargets = identifyPossibleTargets(gameState.getCharacters(), currentUnit);
        Set<Coordinates> openSquares = new HashSet<>();

        // 2.
        for (Character possibleTarget : possibleTargets) {
            // 3.
            for (Coordinates coordinates : possibleTarget.getCoordinates().getAdjacentCoordinates()) {
                // 4.
                if (isOpenSquare(coordinates, gameState)) {
                    // 5.
                    openSquares.add(coordinates);
                }
            }
        }

        // 6.
        return new ArrayList<>(openSquares);
    }
    
    private boolean isOpenSquare(Coordinates coordinates, GameState gameState) {
        // 1. Is it different from terrain OPEN_CAVERN ?
        // 2. Is there an unit on these coordinates ?
        // 3. If answer to both of these question is no then it is an open square

        // 1.
        if (gameState.getCave().getTerrain(coordinates) != TerrainType.OPEN_CAVERN) {
            return false;
        } else {
            // 2.
            for (Character unit : gameState.getCharacters()) {
                if (unit.isDead()) {
                    continue;
                }
                if (coordinates.equals(unit.getCoordinates())) {
                    return false;
                }
            }
        }

        // 3.
        return true;
    }

    private boolean isInRangeOfAnotherUnit(List<Character> units, Character currentUnit) {
        return getTargetsInRange(units, currentUnit).size() != 0;
    }

    private List<Character> getTargetsInRange(List<Character> units, Character currentUnit) {
        // 1. Identify possible targets
        // 2. For each possible target
        // 3. Is it in range ?
        // 4. Add it to set of targets in range
        // 5. Return set

        // 1.
        List<Character> possibleTargets = identifyPossibleTargets(units, currentUnit);
        List<Coordinates> unitAdjacentCoordinates = currentUnit.getCoordinates().getAdjacentCoordinates();
        Set<Character> targetsInRange = new HashSet<>();

        // 2.
        for (Character possibleTarget : possibleTargets) {
            // 3.
            if (unitAdjacentCoordinates.contains(possibleTarget.getCoordinates())) {
                // 4.
                targetsInRange.add(possibleTarget);
            }
        }

        // 5.
        return new ArrayList<>(targetsInRange);
    }

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
                    path.add(current);
                }

                Collections.reverse(path);

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
