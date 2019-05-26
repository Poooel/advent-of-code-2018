package days;

import launcher.ChallengeHelper;
import launcher.Executable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class Day16_ChronalClassification implements Executable {
    @Override
    public String executePartOne() {
        // Read input file
        List<String> input = ChallengeHelper.readInputData(16);
        // Parse part one of the input file
        List<int[][]> puzzleInput = parsePartOneInput(input);
        // Find the frequency of matching opcodes
        return Integer.toString(testOpcodes(puzzleInput));
    }

    @Override
    public String executePartTwo() {
        // Read input file
        List<String> input = ChallengeHelper.readInputData(16);
        // Parse part one of the input file
        List<int[][]> puzzleInput = parsePartOneInput(input);
        // Guess the opcodes from the part one input
        Map<Integer, Integer> opcodes = guessOpcodes(puzzleInput);
        // Then parse the part two of the input file
        List<int[]> testInput = parsePartTwoInput(input);
        // Compute the test program and return the value of the first register
        return Integer.toString(executeTestProgram(testInput,opcodes)[0]);
    }
    
    private int testOpcodes(List<int[][]> puzzleInput) {
        // The frequency counter for the first part of the puzzle
        // to keep track of how many times there is 3 or more opcodes matching the result
        int frequencyCounter = 0;

        // For each input that is made of:
        // - Registers before the instruction has been applied
        // - The instruction
        // - Registers after the instruction has been applied
        for (int[][] input : puzzleInput) {
            // Store registers after the instruction has been applied in a variable for easier access
            int[] after = input[2];

            // Get the registers after each opcode has been applied to the input registers (before registers)
            List<int[]> results = applyOpcodes(input);

            // If the after register shows up 3 or more times in the results list then increment the frequency counter
            // using a custom frequency method instead of Collections.frequency() because it uses .equals() instead of
            // Arrays.equals()
            if (frequency(results, after) >= 3) {
                frequencyCounter++;
            }
        }

        return frequencyCounter;
    }

    private Map<Integer, Integer> guessOpcodes(List<int[][]> puzzleInput) {
        // Map of possibles opcodes for each opcode number
        Map<Integer, Set<Integer>> possiblesOpcodes = new HashMap<>();

        for (int[][] input : puzzleInput) {
            int[] after = input[2];

            List<int[]> results = applyOpcodes(input);

            // If the after registers is found more than zero time in the results list then
            // add the id of each occurence in the result list. We add the position in the list
            // because we know the order in which the opcodes are run.
            if (frequency(results, after) > 0) {
                possiblesOpcodes.compute(input[1][0], (key, value) -> {
                    // If the opcode has not yet been processed then return a new set for the values
                    if (value == null) {
                        return new HashSet<>(occurences(results, after));
                    // If the opcode has already been processed then add the new values using retainAll
                    // to only keep the elements that are in both sets
                    } else {
                        value.retainAll(occurences(results, after));
                        return value;
                    }
                });
            }
        }

        // Map to find out the opcodes.
        // We will store the opcode number (in the instruction) mapping to the index of the result.
        // This map will allow us to remove the opcode that have been already found from other sets
        // (sets from the previous map possiblesOpcodes) to be able to find all the opcodes by elimination.
        // An opcode is found when the set size is 1 which mean there is only one possibility left.
        //
        //  Number in instruction
        //     |
        //     |    Index in the results (we know the order in which opcodes are
        //     |        |                 tested so its the same as having the opcode name)
        //     v        v
        Map<Integer, Integer> uniqueOpcodes = new HashMap<>();

        // while we haven't found all the opcodes (we know there is 16 of them)
        while (uniqueOpcodes.size() != 16) {
            // add all the found opcodes (found opcode mean set size is 1 (there is only one possibility left))
            // in the uniqueOpcodes map
            for (Map.Entry<Integer, Set<Integer>> entry : possiblesOpcodes.entrySet()) {
                if (entry.getValue().size() == 1) {
                    List<Integer> opcodeIndex = new ArrayList<>(entry.getValue());
                    uniqueOpcodes.put(entry.getKey(), opcodeIndex.get(0));
                }
            }

            // remove from the possiblesOpcodes all the confirmed opcodes
            for (Map.Entry<Integer, Integer> uniqueOpcodeEntry : uniqueOpcodes.entrySet()) {
                for (Map.Entry<Integer, Set<Integer>> possibleOpcodeEntry : possiblesOpcodes.entrySet()) {
                    if (!uniqueOpcodeEntry.getKey().equals(possibleOpcodeEntry.getKey())) {
                        possibleOpcodeEntry.getValue().removeAll(Collections.singleton(uniqueOpcodeEntry.getValue()));
                    }
                }
            }
        }

        return uniqueOpcodes;
    }

    private List<int[]> applyOpcodes(int[][] input) {
        int[] before = input[0];
        int[] instruction = input[1];

        int a = instruction[1];
        int b = instruction[2];
        int c = instruction[3];

        List<int[]> results = new ArrayList<>();

        results.add(addr(before, a, b, c));
        results.add(addi(before, a, b, c));
        results.add(mulr(before, a, b, c));
        results.add(muli(before, a, b, c));
        results.add(banr(before, a, b, c));
        results.add(bani(before, a, b, c));
        results.add(borr(before, a, b, c));
        results.add(bori(before, a, b, c));
        results.add(setr(before, a, b, c));
        results.add(seti(before, a, b, c));
        results.add(gtir(before, a, b, c));
        results.add(gtri(before, a, b, c));
        results.add(gtrr(before, a, b, c));
        results.add(eqir(before, a, b, c));
        results.add(eqri(before, a, b, c));
        results.add(eqrr(before, a, b, c));

        return results;
    }

    private int[] executeTestProgram(List<int[]> testInput, Map<Integer, Integer> opcodes) {
        // Initialize a new registers (initialized to 0)
        int[] register = new int[4];

        // For each instruction in the test program
        for (int[] line : testInput) {
            // Extract everything from the instruction
            // use our previous results to map the opcode from the instruction to our opcode number
            int opcode = opcodes.get(line[0]);
            int a = line[1];
            int b = line[2];
            int c = line[3];

            switch (opcode) {
                case 0:
                    register = addr(copy(register), a, b, c);
                    break;
                case 1:
                    register = addi(copy(register), a, b, c);
                    break;
                case 2:
                    register = mulr(copy(register), a, b, c);
                    break;
                case 3:
                    register = muli(copy(register), a, b, c);
                    break;
                case 4:
                    register = banr(copy(register), a, b, c);
                    break;
                case 5:
                    register = bani(copy(register), a, b, c);
                    break;
                case 6:
                    register = borr(copy(register), a, b, c);
                    break;
                case 7:
                    register = bori(copy(register), a, b, c);
                    break;
                case 8:
                    register = setr(copy(register), a, b, c);
                    break;
                case 9:
                    register = seti(copy(register), a, b, c);
                    break;
                case 10:
                    register = gtir(copy(register), a, b, c);
                    break;
                case 11:
                    register = gtri(copy(register), a, b, c);
                    break;
                case 12:
                    register = gtrr(copy(register), a, b, c);
                    break;
                case 13:
                    register = eqir(copy(register), a, b, c);
                    break;
                case 14:
                    register = eqri(copy(register), a, b, c);
                    break;
                case 15:
                    register = eqrr(copy(register), a, b, c);
                    break;
            }
        }

        return register;
    }

    private int frequency(List<int[]> list, int[] test) {
        // The frequency of the test array in the list
        int frequency = 0;

        for (int[] ints : list) {
            // Using Arrays.equals instead of object.equals for a comparison comparing content of the array
            if (Arrays.equals(ints, test)) {
                frequency++;
            }
        }

        return frequency;
    }

    private List<Integer> occurences(List<int[]> list, int[] test) {
        // Store the index of the occurences of test in the list
        List<Integer> occurences = new ArrayList<>();

        // If an array match the test array using Array.equals for the same reason as before, add its index to the list
        for (int i = 0; i < list.size(); i++) {
            if (Arrays.equals(list.get(i), test)) {
                occurences.add(i);
            }
        }

        return occurences;
    }

    private int[] copy(int[] array) {
        return Arrays.copyOf(array, array.length);
    }

    private List<int[][]> parsePartOneInput(List<String> input) {
        List<int[][]> puzzleInput = new ArrayList<>();

        // In my input the first part stops at line 3173 so take a sublist from 0 to 3172 (0 based so 3173 -> 3172)
        input = input.subList(0, 3172);

        // Increment by 4 every time because:
        // - the first line is the before registers
        // - the second line is the instruction
        // - the third line is the after registers
        // - the fourth line is a blank line
        for (int i = 0; i < input.size(); i += 4) {
            puzzleInput.add(new int[][] {
                convertStringToIntArray(extractArray(input.get(i))),
                convertStringToIntArray(input.get(i + 1)),
                convertStringToIntArray(extractArray(input.get(i + 2)))
            });
        }

        return puzzleInput;
    }

    private List<int[]> parsePartTwoInput(List<String> input) {
        List<int[]> sampleProgram = new ArrayList<>();

        // As there is a gap of 3 blank line starts at 3174 to the end to only get input for part 2
        input = input.subList(3174, input.size());

        for (String line : input) {
            sampleProgram.add(convertStringToIntArray(line));
        }

        return sampleProgram;
    }

    /**
     * https://stackoverflow.com/a/18462905/7621349
     */
    private String extractArray(String input) {
        // Split on spaces and keep the limit to 2:
        // "Before: [2, 2, 2, 2]"
        //         ^
        // "Before:", "[2, 2, 2, 2]"
        return input.split(" ", 2)[1]
            // remove opening bracket
            // "2, 2, 2, 2]"
            .replace("[", "")
            // remove closing bracket
            // "2, 2, 2, 2"
            .replace("]", "")
            // remove commas so every number is space separated
            // "2 2 2 2"
            .replace(",", "")
            // trim because some of the entries have space as the beginning
            .trim();
    }

    /**
     * https://stackoverflow.com/a/29219547/7621349
     */
    private int[] convertStringToIntArray(String input) {
        // Split on spaces
        return Stream.of(input.split(" "))
            // Parse each string to an integer
            .mapToInt(Integer::parseInt)
            // convert to an array
            .toArray();
    }

    /**
     * <strong>addr</strong> (add register) stores into register C the result of
     * adding register A and register B.
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] addr(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = registers[a] + registers[b];
        return registers;
    }

    /**
     * <strong>addi</strong> (add immediate) stores into register C the result of
     * adding register A and value B.
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] addi(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = registers[a] + b;
        return registers;
    }

    /**
     * <strong>mulr</strong> (multiply register) stores into register C the result of
     * multiplying register A and register B.
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] mulr(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = registers[a] * registers[b];
        return registers;
    }

    /**
     * <strong>muli</strong> (multiply immediate) stores into register C the result of
     * multiplying register A and value B.
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] muli(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = registers[a] * b;
        return registers;
    }

    /**
     * <strong>banr</strong> (bitwise AND register) stores into register C the result of
     * the bitwise AND of register A and register B.
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] banr(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = registers[a] & registers[b];
        return registers;
    }

    /**
     * <strong>bani</strong> (bitwise AND immediate) stores into register C the result of
     * the bitwise AND of register A and value B.
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] bani(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = registers[a] & b;
        return registers;
    }

    /**
     * <strong>borr</strong> (bitwise OR register) stores into register C the result of
     * the bitwise OR of register A and register B.
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] borr(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = registers[a] | registers[b];
        return registers;
    }

    /**
     * <strong>bori</strong> (bitwise OR immediate) stores into register C the result of
     * the bitwise OR of register A and value B.
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] bori(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = registers[a] | b;
        return registers;
    }

    /**
     * <strong>setr</strong> (set register) copies the contents of register A into register C. (Input B is ignored.)
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] setr(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = registers[a];
        return registers;
    }

    /**
     * <strong>seti</strong> (set immediate) stores value A into register C. (Input B is ignored.)
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] seti(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = a;
        return registers;
    }

    /**
     * <strong>gtir</strong> (greater-than immediate/register) sets register C to 1 if value A is greater than
     * register B. Otherwise, register C is set to 0.
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] gtir(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = a > registers[b] ? 1 : 0;
        return registers;
    }

    /**
     * <strong>gtri</strong> (greater-than register/immediate) sets register C to 1 if register A is greater than
     * value B. Otherwise, register C is set to 0.
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] gtri(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = registers[a] > b ? 1 : 0;
        return registers;
    }

    /**
     * <strong>gtri</strong> (greater-than register/register) sets register C to 1 if register A is greater than
     * register B. Otherwise, register C is set to 0.
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] gtrr(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = registers[a] > registers[b] ? 1 : 0;
        return registers;
    }

    /**
     * <strong>eqir</strong> (equal immediate/register) sets register C to 1 if value A is equal to
     * register B. Otherwise, register C is set to 0.
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] eqir(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = a == registers[b] ? 1 : 0;
        return registers;
    }

    /**
     * <strong>eqri</strong> (equal register/immediate) sets register C to 1 if register A is equal to
     * value B. Otherwise, register C is set to 0.
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] eqri(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = registers[a] == b ? 1 : 0;
        return registers;
    }

    /**
     * <strong>eqrr</strong> (equal register/register) sets register C to 1 if register A is equal to
     * register B. Otherwise, register C is set to 0.
     * @param registers The initial state of the registers
     * @param a The value in the instruction string positioned at index 1
     * @param b The value in the instruction string positioned at index 2
     * @param c The value in the instruction string positioned at index 3
     * @return The state of the registers after the instruction has been executed
     */
    private int[] eqrr(int[] registers, int a, int b, int c) {
        registers = copy(registers);
        registers[c] = registers[a] == registers[b] ? 1 : 0;
        return registers;
    }
}
