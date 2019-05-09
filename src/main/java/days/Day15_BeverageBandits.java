package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Day15_BeverageBandits implements Executable {
    @Override
    public String executePartOne() {
        List<String> input = ChallengeHelper.readInputData(15);
        GameState gameState = parseInitialGameState(input);
        return null;
    }

    @Override
    public String executePartTwo() {
        return null;
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
                        characters.add(new Character(CharacterType.GOBLIN, coordinates));
                        break;
                    case 'E':
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

    private List<Coordinates> identifyOpenSpots(Character currentCharacter, GameState gameState) {
        List<Character> targets = identifyTargets(gameState.getCharacters(), currentCharacter);
        List<Coordinates> openSpots = new ArrayList<>();

        for (Character target : targets) {
            if (gameState.getMap().getTerrain(target.up()) == TerrainType.OPEN_CAVERN) {
                openSpots.add(target.up());
            }
            if (gameState.getMap().getTerrain(target.down()) == TerrainType.OPEN_CAVERN) {
                openSpots.add(target.down());
            }
            if (gameState.getMap().getTerrain(target.left()) == TerrainType.OPEN_CAVERN) {
                openSpots.add(target.left());
            }
            if (gameState.getMap().getTerrain(target.right()) == TerrainType.OPEN_CAVERN) {
                openSpots.add(target.right());
            }
        }

        return openSpots;
    }

    @Value
    private final class GameState {
        private Map map;
        private List<Character> characters;
    }

    @Data
    private final class Character {
        private final CharacterType characterType;
        private final Coordinates coordinates;
        private int hitPoints = 200;
        private final int attackPower = 3;

        Character(CharacterType characterType, Coordinates coordinates) {
            this.characterType = characterType;
            this.coordinates = coordinates;
        }

        void attack(Character target) {
            target.setHitPoints(target.getHitPoints() - this.getAttackPower());
        }

        Coordinates up() {
            return new Coordinates(coordinates.getX(), coordinates.getY() - 1);
        }

        Coordinates down() {
            return new Coordinates(coordinates.getX(), coordinates.getY() + 1);
        }

        Coordinates left() {
            return new Coordinates(coordinates.getX() + 1, coordinates.getY());
        }

        Coordinates right() {
            return new Coordinates(coordinates.getX() - 1, coordinates.getY());
        }

        void moveUp() {
            coordinates.setY(coordinates.getY() - 1);
        }

        void moveDown() {
            coordinates.setY(coordinates.getY() + 1);
        }

        void moveLeft() {
            coordinates.setX(coordinates.getX() + 1);
        }

        void moveRight() {
            coordinates.setX(coordinates.getX() - 1);
        }
    }

    @Data
    @AllArgsConstructor
    private final class Coordinates {
        private int x;
        private int y;

        double distanceTo(Coordinates coordinates) {
            return Math.sqrt(
                Math.pow(coordinates.getX() - this.getX(), 2)
                    +
                Math.pow(coordinates.getY() - this.getY(), 2)
            );
        }
    }

    private final class Map {
        private final TerrainType[][] innerMap;

        Map(int height, int width) {
            innerMap = new TerrainType[height][];

            for (int i = 0; i < width; i++) {
                innerMap[i] = new TerrainType[width];
            }
        }

        TerrainType getTerrain(int x, int y) {
            return innerMap[y][x];
        }

        TerrainType getTerrain(Coordinates coordinates) {
            return getTerrain(coordinates.getX(), coordinates.getY());
        }

        void setTerrain(int x, int y, TerrainType terrainType) {
            innerMap[y][x] = terrainType;
        }

        void setTerrain(Coordinates coordinates, TerrainType terrainType) {
            setTerrain(coordinates.getX(), coordinates.getY(), terrainType);
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
}
