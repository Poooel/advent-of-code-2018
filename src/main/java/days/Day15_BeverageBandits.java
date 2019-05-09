package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.Data;
import lombok.Value;

import java.util.List;

public class Day15_BeverageBandits implements Executable {
    @Override
    public String executePartOne() {
        List<String> input = ChallengeHelper.readInputData(15);
        return null;
    }

    @Override
    public String executePartTwo() {
        return null;
    }

    @Value
    private final class GameState {
        private List<List<PositionType>> map;
        private List<Character> characters;
    }

    @Data
    private final class Character {
        private CharacterType characterType;
        private int x;
        private int y;
        private int hitPoints = 200;
        private final int attackPower = 3;

        Character(CharacterType characterType, int x, int y) {
            this.characterType = characterType;
            this.x = x;
            this.y = y;
        }

        void attack(Character target) {
            target.setHitPoints(target.getHitPoints() - this.getAttackPower());
        }
    }

    private enum PositionType {
        WALLS,
        OPEN_CAVERN
    }

    private enum CharacterType {
        ELF,
        GOBLIN
    }
}
