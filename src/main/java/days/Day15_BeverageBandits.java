package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Day15_BeverageBandits implements Executable {
    @Override
    public String executePartOne() {
        List<String> input = ChallengeHelper.readInputData(15);
        GameState gameState = parseInitialGameState(input);
        return String.format("The result of the combat is %d", play(gameState));
        // This solution doesn't actually work for some obscure reasons
        // Got the solution thanks to https://lamperi.name/aoc/
    }

    @Override
    public String executePartTwo() {
        return null;
        // This solution doesn't actually work for some obscure reasons
        // Got the solution thanks to https://lamperi.name/aoc/
    }

    private int play(GameState gameState) {
        int rounds = 0;
        List<Character> characters = gameState.getCharacters();

        while (true) {
            characters = orderCharacters(characters);

            for (Character character : characters) {
                debug(gameState, rounds);

                if (!character.isDied()) {
                    if (isThereTargets(characters, character)) {
                        if (isInRange(character, characters)) {
                            // attack
                            attackWithCharacter(gameState, character);
                        } else {
                            // move
                            moveCharacter(gameState, character);
                            attackWithCharacter(gameState, character);
                        }
                    } else {
                        // Combat ends
                        int sum = characters
                            .stream()
                            .filter(character1 -> !character1.isDied())
                            .mapToInt(Character::getHitPoints)
                            .sum();

                        for (Character charOverview : characters) {
                            System.out.println(charOverview.toString());
                        }

                        System.out.println(String.format("Sum of hitpoints: %d", sum));
                        System.out.println(String.format("Round: %d", rounds));
                        return sum * rounds;
                    }
                }
            }

            rounds++;

            characters = characters.stream().filter(character -> !character.isDied()).collect(Collectors.toList());
        }
    }

    private void moveCharacter(GameState gameState, Character character) {
        // Target open spots
        List<Coordinates> openSpots = identifyOpenSpots(character, gameState);
        // Character open spots
        List<Coordinates> characterOpenSpots = identifyCharacterOpenSpots(character, gameState);

        // If no target can be reached
        if (openSpots.isEmpty()) {
            return;
        }

        // If the character has no open spot to move to (around him)
        if (characterOpenSpots.isEmpty()) {
            return;
        }

        // Map of int and list of list
        // int is the length of the path
        // list of list store the path to the goal, list of list because there can be multiple path for a same length
        java.util.Map<Integer, List<List<Coordinates>>> pathsToTarget = new HashMap<>();

        for (Coordinates openSpot : openSpots) {
            for (Coordinates characterOpenSpot : characterOpenSpots) {
                List<Coordinates> path = findShortestPathWithAStar(characterOpenSpot, openSpot, gameState);

                if (path == null) {
                    continue;
                }

                if (!pathsToTarget.containsKey(path.size())) {
                    pathsToTarget.put(path.size(), new ArrayList<>());
                }

                pathsToTarget.get(path.size()).add(path);
            }
        }

        if (pathsToTarget.isEmpty()) {
            return;
        }

        List<List<Coordinates>> shortestPaths = Collections.min(pathsToTarget.entrySet(), Comparator.comparing(java.util.Map.Entry::getKey)).getValue();

        java.util.Map<Coordinates, List<Coordinates>> tiedPaths = new HashMap<>();

        for (List<Coordinates> shortestPath : shortestPaths) {
            tiedPaths.put(shortestPath.get(shortestPath.size() - 1), shortestPath);
        }

        List<Coordinates> bestPath = tiedPaths.get(findTopCoordinates(tiedPaths.keySet()));

        character.moveTo(bestPath.get(0));
    }

    private void attackWithCharacter(GameState gameState, Character character) {
        List<Character> targetsInRange = getTargetsInRange(character, gameState.getCharacters());

        if (targetsInRange.isEmpty()) {
            return;
        }

        int minHitPoints = Integer.MAX_VALUE;
        List<Character> targetsWithMinHitPoints = new ArrayList<>();

        for (Character target : targetsInRange) {
            if (target.getHitPoints() < minHitPoints) {
                minHitPoints = target.getHitPoints();
                targetsWithMinHitPoints.clear();
                targetsWithMinHitPoints.add(target);
            } else if (target.getHitPoints() == minHitPoints) {
                targetsWithMinHitPoints.add(target);
            }
        }

        targetsWithMinHitPoints = orderCharacters(targetsWithMinHitPoints);

        if (character.attack(targetsWithMinHitPoints.get(0))) {
            gameState.getCharacters().remove(targetsWithMinHitPoints.get(0));
            targetsWithMinHitPoints.get(0).setDied(true);
        }
    }

    private GameState parseInitialGameState(List<String> input) {
        Map map = new Map(input.size(), input.get(0).length());
        List<Character> characters = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            String line = input.get(i);

            for (int j = 0; j < line.length(); j++) {
                char terrain = line.charAt(j);
                Coordinates coordinates = new Coordinates(j, i);

                switch (terrain) {
                    case '#':
                        map.setTerrain(coordinates, TerrainType.WALL);
                        break;
                    case '.':
                        map.setTerrain(coordinates, TerrainType.OPEN_CAVERN);
                        break;
                    case 'G':
                        map.setTerrain(coordinates, TerrainType.OPEN_CAVERN);
                        characters.add(new Character(CharacterType.GOBLIN, coordinates));
                        break;
                    case 'E':
                        map.setTerrain(coordinates, TerrainType.OPEN_CAVERN);
                        characters.add(new Character(CharacterType.ELF, coordinates));
                        break;
                }
            }
        }

        return new GameState(map, characters);
    }

    private boolean isThereTargets(List<Character> characters, Character currentCharacter) {
        return characters
            .stream()
            .filter(character -> !character.isDied())
            .anyMatch(character -> character.getCharacterType() != currentCharacter.getCharacterType());
    }

    private List<Character> identifyTargets(List<Character> characters, Character currentCharacter) {
        return characters
            .stream()
            .filter(character -> character.getCharacterType() != currentCharacter.getCharacterType())
            .collect(Collectors.toList());
    }

    private List<Character> orderCharacters(List<Character> characters) {
        Comparator<Character> comparator = Comparator.comparing(character -> character.getCoordinates().getY());
        comparator = comparator.thenComparing(character -> character.getCoordinates().getX());

        return characters
            .stream()
            .sorted(comparator)
            .collect(Collectors.toList());
    }

    private Coordinates findTopCoordinates(Set<Coordinates> coordinates) {
        Comparator<Coordinates> comparator = Comparator.comparing(Coordinates::getY);
        comparator = comparator.thenComparing(Coordinates::getX);

        return coordinates
            .stream()
            .min(comparator)
            .get();
    }

    private List<Coordinates> identifyOpenSpots(Character currentCharacter, GameState gameState) {
        List<Character> targets = identifyTargets(gameState.getCharacters(), currentCharacter);
        List<Coordinates> openSpots = new ArrayList<>();

        for (Character target : targets) {
            if (isOpenSpot(gameState, target.getCoordinates().up())) {
                openSpots.add(target.getCoordinates().up());
            }
            if (isOpenSpot(gameState, target.getCoordinates().down())) {
                openSpots.add(target.getCoordinates().down());
            }
            if (isOpenSpot(gameState, target.getCoordinates().left())) {
                openSpots.add(target.getCoordinates().left());
            }
            if (isOpenSpot(gameState, target.getCoordinates().right())) {
                openSpots.add(target.getCoordinates().right());
            }
        }

        return openSpots;
    }

    private List<Coordinates> identifyCharacterOpenSpots(Character currentCharacter, GameState gameState) {
        List<Coordinates> openSpots = new ArrayList<>();

        if (isOpenSpot(gameState, currentCharacter.getCoordinates().up())) {
            openSpots.add(currentCharacter.getCoordinates().up());
        }
        if (isOpenSpot(gameState, currentCharacter.getCoordinates().down())) {
            openSpots.add(currentCharacter.getCoordinates().down());
        }
        if (isOpenSpot(gameState, currentCharacter.getCoordinates().left())) {
            openSpots.add(currentCharacter.getCoordinates().left());
        }
        if (isOpenSpot(gameState, currentCharacter.getCoordinates().right())) {
            openSpots.add(currentCharacter.getCoordinates().right());
        }

        return openSpots;
    }

    private boolean isOpenSpot(GameState gameState, Coordinates coordinates) {
        if (gameState.getMap().getTerrain(coordinates) != TerrainType.OPEN_CAVERN) {
            return false;
        }

        for (Character character : gameState.getCharacters()) {
            if (coordinates.equals(character.getCoordinates())) {
                return false;
            }
        }

        return true;
    }

    private boolean isInRange(Character currentCharacter, List<Character> characters) {
        for (Character character : characters) {
            if (character == currentCharacter) {
                continue;
            }
            if (character.isDied()) {
                continue;
            }
            if (character.getCharacterType() == currentCharacter.getCharacterType()) {
                continue;
            }
            if (character.getCoordinates().up().equals(currentCharacter.getCoordinates())) {
                return true;
            }
            if (character.getCoordinates().down().equals(currentCharacter.getCoordinates())) {
                return true;
            }
            if (character.getCoordinates().left().equals(currentCharacter.getCoordinates())) {
                return true;
            }
            if (character.getCoordinates().right().equals(currentCharacter.getCoordinates())) {
                return true;
            }
        }

        return false;
    }

    private List<Character> getTargetsInRange(Character currentCharacter, List<Character> characters) {
        List<Character> targetsInRange = new ArrayList<>();

        for (Character character : characters) {
            if (character == currentCharacter) {
                continue;
            }
            if (character.getCharacterType() == currentCharacter.getCharacterType()) {
                continue;
            }
            if (character.getCoordinates().up().equals(currentCharacter.getCoordinates())) {
                targetsInRange.add(character);
            }
            if (character.getCoordinates().down().equals(currentCharacter.getCoordinates())) {
                targetsInRange.add(character);
            }
            if (character.getCoordinates().left().equals(currentCharacter.getCoordinates())) {
                targetsInRange.add(character);
            }
            if (character.getCoordinates().right().equals(currentCharacter.getCoordinates())) {
                targetsInRange.add(character);
            }
        }

        return targetsInRange;
    }

    /**
     * https://en.wikipedia.org/wiki/A*_search_algorithm
     */
    private List<Coordinates> findShortestPathWithAStar(Coordinates start, Coordinates goal, GameState gameState) {
        Set<Coordinates> closedSet = new HashSet<>();
        Set<Coordinates> openSet = new HashSet<>();
        java.util.Map<Coordinates, Coordinates> cameFrom = new HashMap<>();
        java.util.Map<Coordinates, Integer> gScore = new HashMap<>();
        java.util.Map<Coordinates, Integer> fScore = new HashMap<>();

        openSet.add(start);
        gScore.put(start, 0);
        fScore.put(start, start.distanceTo(goal));

        while (!openSet.isEmpty()) {
            Coordinates current = getLowestCoordinatesScore(fScore, openSet);

            if (current.equals(goal)) {
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

            List<Coordinates> neighbors = new ArrayList<>();

            if (isOpenSpot(gameState, current.up())) {
                neighbors.add(current.up());
            }
            if (isOpenSpot(gameState, current.left())) {
                neighbors.add(current.left());
            }
            if (isOpenSpot(gameState, current.right())) {
                neighbors.add(current.right());
            }
            if (isOpenSpot(gameState, current.down())) {
                neighbors.add(current.down());
            }

            for (Coordinates neighbor : neighbors) {
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
                fScore.put(neighbor, gScore.get(neighbor) + neighbor.distanceTo(goal));
            }
        }

        return null;
    }

    private Coordinates getLowestCoordinatesScore(java.util.Map<Coordinates, Integer> fScore, Set<Coordinates> openSet) {
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

    @Value
    private final class GameState {
        private Day15_BeverageBandits.Map map;
        private List<Character> characters;
    }

    @Data
    private final class Character {
        private final CharacterType characterType;
        private Coordinates coordinates;
        private int hitPoints = 200;
        private final int attackPower = 3;
        private boolean died;

        Character(CharacterType characterType, Coordinates coordinates) {
            this.characterType = characterType;
            this.coordinates = coordinates;
        }

        boolean attack(Character target) {
            target.setHitPoints(target.getHitPoints() - this.getAttackPower());
            return target.getHitPoints() <= 0;
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
    }

    private final class Map {
        private final TerrainType[][] rawMap;

        Map(int height, int width) {
            rawMap = new TerrainType[height][];

            for (int i = 0; i < height; i++) {
                rawMap[i] = new TerrainType[width];
            }
        }

        TerrainType getTerrain(int x, int y) {
            return rawMap[y][x];
        }

        TerrainType getTerrain(Coordinates coordinates) {
            return getTerrain(coordinates.getX(), coordinates.getY());
        }

        void setTerrain(int x, int y, TerrainType terrainType) {
            rawMap[y][x] = terrainType;
        }

        void setTerrain(Coordinates coordinates, TerrainType terrainType) {
            setTerrain(coordinates.getX(), coordinates.getY(), terrainType);
        }

        TerrainType[][] getRawMap() {
            return rawMap;
        }
    }

    private enum TerrainType {
        WALL,
        OPEN_CAVERN
    }

    private enum CharacterType {
        ELF,
        GOBLIN
    }

    private void debug(GameState gameState, int rounds) {
        int height = gameState.getMap().getRawMap().length;
        int width = gameState.getMap().getRawMap()[0].length;

        char[][] debugMap = new char[height][];

        for (int i = 0; i < height; i++) {
            debugMap[i] = new char[width];
            for (int j = 0; j < width; j++) {
                switch (gameState.getMap().getTerrain(j, i)) {
                    case WALL:
                        debugMap[i][j] = '#';
                        break;
                    case OPEN_CAVERN:
                        debugMap[i][j] = '.';
                        break;
                }
            }
        }

        for (Character character : gameState.getCharacters()) {
            if (!character.isDied()) {
                switch (character.getCharacterType()) {
                    case ELF:
                        debugMap[character.getCoordinates().getY()][character.getCoordinates().getX()] = 'E';
                        break;
                    case GOBLIN:
                        debugMap[character.getCoordinates().getY()][character.getCoordinates().getX()] = 'G';
                        break;
                }
            }
        }

        System.out.println(String.format("Round: %d", rounds));

        for (int i = 0; i < height; i++) {
            System.out.println(new String(debugMap[i]));
        }

        System.out.println();
    }
}
