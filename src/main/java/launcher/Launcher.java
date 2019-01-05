package launcher;

import days.AlchemicalReduction;
import days.ChronalCalibration;
import days.ChronalCoordinates;
import days.InventoryManagementSystem;
import days.NoMatterHowYouSliceIt;
import days.NotDoneYet;
import days.ReposeRecord;
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
                return new ChronalCalibration();
            case 2:
                return new InventoryManagementSystem();
            case 3:
                return new NoMatterHowYouSliceIt();
            case 4:
                return new ReposeRecord();
            case 5:
                return new AlchemicalReduction();
            case 6:
                return new ChronalCoordinates();
            default:
                return new NotDoneYet();
        }
    }
}
