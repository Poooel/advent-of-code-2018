package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReposeRecord implements Executable {
    @Override
    public String executePartOne() {
        List<String> inputs = ChallengeHelper.readInputData(4);

        // We can just sort the inputs, with a default sorting because the date format
        // is sortable
        Collections.sort(inputs);

        // Parse the input to get the guards
        Map<Integer, Guard> guards = parseInput(inputs);

        // Get the guard most asleep
        Guard mostAsleepGuard = findMostAsleepGuard(guards);
        // Find the minute he most slept on
        int mostAsleepMinute = findMostAsleepMinute(mostAsleepGuard).getIndex();

        return String.valueOf(mostAsleepGuard.getId() * mostAsleepMinute);
    }

    @Override
    public String executePartTwo() {
        List<String> inputs = ChallengeHelper.readInputData(4);

        // We can just sort the inputs, with a default sorting because the date format
        // is sortable
        Collections.sort(inputs);

        // Parse the input to get the guards
        Map<Integer, Guard> guards = parseInput(inputs);

        // Get a list of context for each guard
        List<MaximumContext> maximumContexts = new ArrayList<>();

        for (Guard guard : guards.values()) {
            // Compute the most aslept minute for each guard
            maximumContexts.add(findMostAsleepMinute(guard));
        }

        // Get the context with the most time asleep on the minute
        MaximumContext maximumContext = maximumContexts
            // Stream the list of context
            .stream()
            // Use max operation using the getMaximum of the MaximumContext object to compare
            .max(Comparator.comparingInt(MaximumContext::getMaximum))
            // Get because max return an optional
            .get();

        return String.valueOf(maximumContext.getGuardId() * maximumContext.getIndex());
    }

    private Map<Integer, Guard> parseInput(List<String> inputs) {
        Map<Integer, Guard> guards = new HashMap<>();

        for (int i = 0; i < inputs.size(); i++) {
            // Split the input on the space between the date and the info
            // [1518-11-01 00:00] Guard #10 begins shift
            //                   ↥
            //               This space
            String[] splitInput = inputs.get(i).split(" (?=[Gfw])");

            if (splitInput[1].contains("Guard")) {
                // Get the second part of the split where the guard Id is
                int guardId = getGuardId(splitInput[1]);

                // Initialize a new guard
                Guard guard = new Guard(guardId, new ArrayList<>());

                // if we already processed that guard before, replace the initialized guard
                // with the one from the map
                if (guards.containsKey(guardId)) {
                    guard = guards.get(guardId);
                }

                // Create a new list of shifts
                List<Shift> shifts = new ArrayList<>();
                String beginningOfShift = null;

                for (int j = i + 1; j < inputs.size(); j++) {
                    // Split the input as before
                    String[] splitShift = inputs.get(j).split(" (?=[Gfw])");

                    // If the input contains falls, it means the guard just fell asleep
                    if (splitShift[1].contains("falls")) {
                        // So we get the date of when he fell asleep
                        beginningOfShift = splitShift[0];
                        // If the input contains wakes, it means the guard just woke up
                    } else if (splitShift[1].contains("wakes")) {
                        // So create a new shift with the previously acquired beginning date and
                        // the woke up date
                        shifts.add(
                            computeShift(beginningOfShift, splitShift[0])
                        );
                        // If the input contains guard, it means we changed guard
                    } else if (splitShift[1].contains("Guard")) {
                        // Decrement the previous loop, so the outer loop will see what we just saw
                        i = j - 1;
                        // Add all the shifts we computed to the guard shifts
                        guard.getShifts().addAll(mergeShifts(shifts));
                        // Put the guard in the list if we didn't already put it in
                        guards.putIfAbsent(guardId, guard);
                        // break the loop and give back control to outer loop
                        break;
                    }
                }
            }
        }

        return guards;
    }

    private int getGuardId(String guardInfoString) {
        return Integer.parseInt(guardInfoString
            // Split it on the spaces
            // "Guard #10 begins shift" -> ["Guard", "#10", "begins", "shift"]
            .split(" ")
            // Take the second part as it is where the Id is
            // ["Guard", "#10", "begins", "shift"]
            //             ↥
            [1]
            // Remove the # at the beginning
            // "#10"
            //  ↥
            .substring(1));
    }

    private Shift computeShift(String beginning, String end) {
        String date = getDateFromShiftString(beginning);
        int beginningShift = getMinutesFromShiftString(beginning);
        int endShift = getMinutesFromShiftString(end);
        List<Boolean> minutes = initializeMinutesArray();

        // Set to true the minutes where the guard is asleep
        for (int i = beginningShift; i < endShift; i++) {
            minutes.set(i, true);
        }

        return new Shift(
            minutes,
            date
        );
    }

    private Shift mergeShift(Shift firstShift, Shift secondShift) {
        List<Boolean> mergedMinutes = initializeMinutesArray();
        List<Boolean> firstMinutes = firstShift.getAsleepMinutes();
        List<Boolean> secondMinutes = secondShift.getAsleepMinutes();

        for (int i = 0; i < mergedMinutes.size(); i++) {
            // if the value from the first shift or second shift is true
            // then the merged value is true
            if (firstMinutes.get(i) || secondMinutes.get(i)) {
                mergedMinutes.set(i, true);
            }
        }

        return new Shift(
            mergedMinutes,
            firstShift.getDate()
        );
    }

    private List<Boolean> initializeMinutesArray() {
        List<Boolean> minutes = new ArrayList<>();

        // Initialize the list with 60 minutes of awake guard
        // true -> asleep, false -> awake
        for (int i = 0; i < 60; i++) {
            minutes.add(false);
        }

        return minutes;
    }

    private int getMinutesFromShiftString(String shiftString) {
        return Integer.parseInt(
            shiftString
                // Split on the space
                // "[1518-11-01 00:00]" -> ["[1518-11-01", "00:00]"]
                .split(" ")
                // Take the second part where the time is
                [1]
                // Split on the semi-colon
                // "00:00" -> ["00", "00"]
                .split(":")
                // Takes the second part where the minutes are
                [1]
                // Remove the "]"
                .substring(0, 2)
        );
    }

    private String getDateFromShiftString(String shiftString) {
        return shiftString
            // Split on the space
            // "[1518-11-01 00:00]" -> ["[1518-11-01", "00:00]"]
            .split(" ")
            // Take the first part where the date is
            [0]
            // Remove the "["
            .substring(1);
    }

    private int getIndexOfSameDateShift(Shift shiftToCompare, List<Shift> shifts) {
        // Find two shift with the same date
        for (int i = 0; i < shifts.size(); i++) {
            // If the two shift are equals, continue because we are comparing to itself
            if (shiftToCompare.equals(shifts.get(i))) {
                continue;
            }

            // If the two dates are equal return the index in the shifts list
            if (shiftToCompare.getDate().equals(shifts.get(i).getDate())) {
                return i;
            }
        }

        // If we found nothing return -1
        return -1;
    }

    private List<Shift> mergeShifts(List<Shift> shifts) {
        // Initialize to true to enter the loop at least one time,
        // could have used a do...while() instead
        boolean mergedSomething = true;

        while (mergedSomething) {
            // If we didn't merge anything, then it will stop next time,
            // the mergedSomething is evalued
            mergedSomething = false;
            for (int i = 0; i < shifts.size(); i++) {
                // Find a duplicate date
                int index = getIndexOfSameDateShift(shifts.get(i), shifts);

                // If the index is different from -1, which means we found something
                if (index != -1) {
                    // Assign the merged shift to the first shift
                    shifts.set(i, mergeShift(shifts.get(i), shifts.get(index)));
                    // Delete the second shift
                    shifts.remove(index);
                    // Turn flag to true to signal we merged something
                    mergedSomething = true;
                }
            }
        }

        // Return the new list of merged shifts if we did something, else the original list
        return shifts;
    }

    private Guard findMostAsleepGuard(Map<Integer, Guard> guards) {
        // Initialize the maximum as the smallest value possible to be sure we don't miss
        // any value, could have taken the first value of the list too
        int maximum = Integer.MIN_VALUE;
        // Initialize the most asleep guard to null
        Guard mostAsleepGuard = null;

        // For each guard
        for (Guard guard : guards.values()) {
            // Initialize a local maximum to compare later
            int localMaximum = Integer.MIN_VALUE;

            // For each shift add the time slept by the guard
            for (Shift shift : guard.getShifts()) {
                localMaximum += shift
                    // Get the asleep minutes of the shift of the guard
                    .getAsleepMinutes()
                    // Stream it
                    .stream()
                    // Filter the minutes where the guard is awake (remember awake == false)
                    .filter(asleep -> asleep)
                    // Count them
                    .count();
            }

            // If the local maximum is greather than the maximum
            if (localMaximum > maximum) {
                // replace the maximum with the local maximum
                maximum = localMaximum;
                // store the most asleep guard
                mostAsleepGuard = guard;
            }
        }

        return mostAsleepGuard;
    }

    private MaximumContext findMostAsleepMinute(Guard guard) {
        // Initialize a list of counter of minutes, at the beginning,
        // every minute is equal to 0, because we haven't processed the sleeping time of the
        // guard yet
        List<Integer> asleepMinuteCount = new ArrayList<>();

        for (int i = 0; i < 60; i++) {
            asleepMinuteCount.add(0);
        }

        // For each shift in the guard shifts
        for (Shift shift : guard.getShifts()) {
            // For each minutes in the shift
            for (int i = 0; i < shift.getAsleepMinutes().size(); i++) {
                // if the guard slept on that minute, increment that minute in the list
                if (shift.getAsleepMinutes().get(i)) {
                    asleepMinuteCount.set(i, asleepMinuteCount.get(i) + 1);
                }
            }
        }

        // Initialize the maximum to the smallest value as before
        int maximum = Integer.MIN_VALUE;
        int index = -1;

        // Find the most slept minute in the list with its index
        for (int i = 0; i < asleepMinuteCount.size(); i++) {
            if (asleepMinuteCount.get(i) > maximum) {
                maximum = asleepMinuteCount.get(i);
                index = i;
            }
        }

        // Return the result as an object to get multiple informations instead
        // of copy pasting this function with a different return value
        return new MaximumContext(index, maximum, guard.getId());
    }

    @Value
    private class Guard {
        private int id;
        private List<Shift> shifts;
    }

    @Value
    private class MaximumContext {
        private int index;
        private int maximum;
        private int guardId;
    }

    @Value
    private class Shift {
        private List<Boolean> asleepMinutes;
        private String date;
    }
}
