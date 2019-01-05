package launcher;

import days.Day5_AlchemicalReduction;
import days.Day1_ChronalCalibration;
import days.Day6_ChronalCoordinates;
import days.Day2_InventoryManagementSystem;
import days.Day3_NoMatterHowYouSliceIt;
import days.Day0_NotDoneYet;
import days.Day4_ReposeRecord;
import lombok.SneakyThrows;

public class Launcher {
    @SneakyThrows
    public static void main(String[] args) {
        System.out.println("Welcome to the Advent of Code 2018.");

        System.out.println("Choose a day to begin: (1-25)");
        int choosenDay = LauncherHelper.getValidIntegerInput(
            "Please input a day between 1 and 25.",
            1,
            25
        );

        System.out.println("Choose a part for the day: (1-2)");
        int choosenPart = LauncherHelper.getValidIntegerInput(
            "Please a part between 1 and 2.",
            1,
            2
        );

        if (choosenPart == 1) {
            System.out.println(
                String.format(
                    "The answer for Day %02d Part %d is: %s",
                    choosenDay,
                    choosenPart,
                    getCorrespondingExecutableDay(choosenDay).executePartOne()
                )
            );
        } else {
            System.out.println(
                String.format(
                    "The answer for Day %02d Part %d is: %s",
                    choosenDay,
                    choosenPart,
                    getCorrespondingExecutableDay(choosenDay).executePartTwo()
                )
            );
        }
    }

    private static Executable getCorrespondingExecutableDay(int day) {
        switch (day) {
            case 1:
                return new Day1_ChronalCalibration();
            case 2:
                return new Day2_InventoryManagementSystem();
            case 3:
                return new Day3_NoMatterHowYouSliceIt();
            case 4:
                return new Day4_ReposeRecord();
            case 5:
                return new Day5_AlchemicalReduction();
            case 6:
                return new Day6_ChronalCoordinates();
            default:
                return new Day0_NotDoneYet();
        }
    }
}
