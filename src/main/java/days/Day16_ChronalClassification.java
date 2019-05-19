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
        List<String> input = ChallengeHelper.readInputData(16);
        List<int[][]> puzzleInput = parsePartOneInput(input);
        return Integer.toString(testOpCodes(puzzleInput));
    }

    @Override
    public String executePartTwo() {
        List<String> input = ChallengeHelper.readInputData(16);
        List<int[][]> puzzleInput = parsePartOneInput(input);
        Map<Integer, String> opCodes = guessOpCodes(puzzleInput);
        List<int[]> testInput = parsePartTwoInput(input);
        return Integer.toString(executeTestProgram(testInput,opCodes)[0]);
    }
    
    private int testOpCodes(List<int[][]> puzzleInput) {
        int frequencyCounter = 0;

        for (int[][] input : puzzleInput) {
            int[] after = input[2];

            List<int[]> results = applyOpCodes(input);

            if (frequency(results, after) >= 3) {
                frequencyCounter++;
            }
        }

        return frequencyCounter;
    }

    private Map<Integer, String> guessOpCodes(List<int[][]> puzzleInput) {
        Map<Integer, String> opCodes = new HashMap<>();

        String[] opCodesNames = new String[] {
            "addr",
            "addi",
            "mulr",
            "muli",
            "banr",
            "bani",
            "borr",
            "bori",
            "setr",
            "seti",
            "gtir",
            "gtri",
            "gtrr",
            "eqir",
            "eqri",
            "eqrr"
        };

        Map<Integer, Set<Integer>> possiblesOpCodes = new HashMap<>();

        for (int[][] input : puzzleInput) {
            int[] after = input[2];

            List<int[]> results = applyOpCodes(input);

            if (frequency(results, after) >= 1) {
                possiblesOpCodes.compute(input[1][0], (key, value) -> {
                    if (value == null) {
                        return new HashSet<>(occurences(results, after));
                    } else {
                        value.retainAll(occurences(results, after));
                        return value;
                    }
                });
            }
        }

        Map<Integer, Integer> uniqueOpCodes = new HashMap<>();

        while (uniqueOpCodes.size() != 16) {
            for (Map.Entry<Integer, Set<Integer>> entry : possiblesOpCodes.entrySet()) {
                if (entry.getValue().size() == 1) {
                    List<Integer> opCodeIndex = new ArrayList<>(entry.getValue());
                    uniqueOpCodes.put(entry.getKey(), opCodeIndex.get(0));
                }
            }

            for (Map.Entry<Integer, Integer> uniqueOpCodeEntry : uniqueOpCodes.entrySet()) {
                for (Map.Entry<Integer, Set<Integer>> possibleOpCodeEntry : possiblesOpCodes.entrySet()) {
                    if (!uniqueOpCodeEntry.getKey().equals(possibleOpCodeEntry.getKey())) {
                        possibleOpCodeEntry.getValue().removeAll(Collections.singleton(uniqueOpCodeEntry.getValue()));
                    }
                }
            }
        }

        for (Map.Entry<Integer, Set<Integer>> possibleOpCodeEntry : possiblesOpCodes.entrySet()) {
            opCodes.put(possibleOpCodeEntry.getKey(), opCodesNames[possibleOpCodeEntry.getValue().iterator().next()]);
        }

        return opCodes;
    }

    private List<int[]> applyOpCodes(int[][] input) {
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

    private int[] executeTestProgram(List<int[]> testInput, Map<Integer, String> opCodes) {
        int[] register = new int[4];

        for (int[] line : testInput) {
            String opCode = opCodes.get(line[0]);
            int a = line[1];
            int b = line[2];
            int c = line[3];

            switch (opCode) {
                case "addr":
                    register = addr(copy(register), a, b, c);
                    break;
                case "addi":
                    register = addi(copy(register), a, b, c);
                    break;
                case "mulr":
                    register = mulr(copy(register), a, b, c);
                    break;
                case "muli":
                    register = muli(copy(register), a, b, c);
                    break;
                case "banr":
                    register = banr(copy(register), a, b, c);
                    break;
                case "bani":
                    register = bani(copy(register), a, b, c);
                    break;
                case "borr":
                    register = borr(copy(register), a, b, c);
                    break;
                case "bori":
                    register = bori(copy(register), a, b, c);
                    break;
                case "setr":
                    register = setr(copy(register), a, b, c);
                    break;
                case "seti":
                    register = seti(copy(register), a, b, c);
                    break;
                case "gtir":
                    register = gtir(copy(register), a, b, c);
                    break;
                case "gtri":
                    register = gtri(copy(register), a, b, c);
                    break;
                case "gtrr":
                    register = gtrr(copy(register), a, b, c);
                    break;
                case "eqir":
                    register = eqir(copy(register), a, b, c);
                    break;
                case "eqri":
                    register = eqri(copy(register), a, b, c);
                    break;
                case "eqrr":
                    register = eqrr(copy(register), a, b, c);
                    break;
            }
        }

        return register;
    }

    private int frequency(List<int[]> list, int[] test) {
        int frequency = 0;

        for (int[] ints : list) {
            if (Arrays.equals(ints, test)) {
                frequency++;
            }
        }

        return frequency;
    }

    private List<Integer> occurences(List<int[]> list, int[] test) {
        List<Integer> occurences = new ArrayList<>();

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

        input = input.subList(0, 3172);

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
        return input.split(" ", 2)[1]
            .replace("[", "")
            .replace("]", "")
            .replace(",", "")
            .trim();
    }

    /**
     * https://stackoverflow.com/a/29219547/7621349
     */
    private int[] convertStringToIntArray(String input) {
        return Stream.of(input.split(" "))
            .mapToInt(Integer::parseInt)
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
