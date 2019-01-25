package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Day08_MemoryManeuver implements Executable {
    @Override
    public String executePartOne() {
        List<String> inputs = ChallengeHelper.readInputData(8);
        // Convert the strings to integers
        List<Integer> numbers = parseInput(inputs);
        // Build the tree from the input
        Node tree = buildTreeRecursively(numbers);
        // Walk the tree and make the sum of the metadata
        return String.valueOf(sumOfMetadataRecursively(tree));
    }

    @Override
    public String executePartTwo() {
        List<String> inputs = ChallengeHelper.readInputData(8);
        // Convert the strings to integers
        List<Integer> numbers = parseInput(inputs);
        // Build the tree from the input
        Node tree = buildTreeRecursively(numbers);
        // Walk the tree and make the sum of the nodes according to the rules
        return String.valueOf(complicatedSumOfMetadataRecursively(tree));
    }

    private List<Integer> parseInput(List<String> inputs) {
        List<Integer> numbers = new ArrayList<>();

        // We use a for loop, but there should be only one line in the input file
        for (String input : inputs) {
            // Split the line on the spaces
            String[] splitInput = input.split(" ");
            // Convert the array to a list
            List<String> splitInputList = Arrays.asList(splitInput);

            // Add all the parsed numbers to the list
            numbers.addAll(
                splitInputList
                    // Stream the input list
                    .stream()
                    // Parse the string to an integer
                    .mapToInt(Integer::parseInt)
                    // Box them to get a Stream<Integer> instead of IntStream
                    .boxed()
                    // Collect the result into a list
                    .collect(Collectors.toList())
            );
        }

        return numbers;
    }

    private int sumOfMetadataRecursively(Node rootNode) {
        // Store the local sum
        int localSum = 0;

        // Add the sum of each child of this node
        for (Node child : rootNode.getChilds()) {
            localSum += sumOfMetadataRecursively(child);
        }

        // Return the sum of each child plus the sum of the metadata of the current node
        return localSum + rootNode.getMetadata().stream().mapToInt(Integer::intValue).sum();
    }

    private int complicatedSumOfMetadataRecursively(Node rootNode) {
        // If the node doesn't have any childs
        if (rootNode.getChilds().size() == 0) {
            // Do the sum of its metadata
            return rootNode.getMetadata().stream().mapToInt(Integer::intValue).sum();
        } else {
            // Store the local sum
            int localSum = 0;

            // For each metadata number, we use it as an index (the index is 1 based,
            // so we need to substract 1 from it to be able to use it)
            for (Integer metadata : rootNode.getMetadata()) {
                // If the index is 0 then we can't use it so continue
                if (metadata == 0) {
                    continue;
                // If the index is bigger than the size of the childs we can't use it so continue
                } else if (metadata > rootNode.getChilds().size()) {
                    continue;
                } else {
                    // If we can use the index, get the child corresponding to this index
                    // and add the result to the local sum
                    localSum += complicatedSumOfMetadataRecursively(
                        rootNode.getChilds().get(
                            metadata - 1
                        )
                    );
                }
            }

            // When we processed all metadata entries return the sum
            return localSum;
        }
    }

    private Node buildTreeRecursively(List<Integer> numbers) {
        // Extract the header from the input, we know it is always the first two numbers
        List<Integer> header = numbers.subList(0, 2);
        // The first part is the number of childs
        int childs = header.get(0);
        // The second part is the number of metadata entries
        int entries = header.get(1);

        // Remove the header from the input
        numbers.subList(0, 2).clear();

        // List to hold all child nodes
        List<Node> childList = new ArrayList<>();

        // For each child node run the method recursively
        for (int i = 0; i < childs; i++) {
            // Add each child to the child list
            childList.add(buildTreeRecursively(numbers));
        }

        // Get the metadata from the input
        List<Integer> metadataEntries = new ArrayList<>(numbers.subList(0, entries));

        // Then remove the metadata entries
        numbers.subList(0, entries).clear();

        // Return a new node with the childs and metadata entries
        return new Node(childList, metadataEntries);
    }

    @Value
    private class Node {
        private List<Node> childs;
        private List<Integer> metadata;
    }
}
