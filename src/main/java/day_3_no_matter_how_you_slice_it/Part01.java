package day_3_no_matter_how_you_slice_it;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Part01 {
    public static void main(String[] args) throws IOException {
        System.out.println(String.format("The answer is: %s",
            // Parse the input
            parseInput(
                // Read all the lines of the file
                Files.readAllLines(
                    Paths.get("input/day_03.input")
                )
            )
                // Get the List of List of List of String which represent the fabric
                .getFabric()
                // Stream it
                .stream()
                // Stream the inner list
                .map(Collection::stream)
                // Flat map the stream of stream into a single stream
                .flatMap(x -> x)
                // Filter to get only the point where the claim overlaps
                .filter(x -> x.size() > 1)
                // Count the overlaps
                .count()
            )
        );
    }

    public static ParseContext parseInput(List<String> inputs) {
        // Create a list of list of list of string which represent the fabric
        // The first list is for the width
        // The second list is for the height
        // The third list is to store the id of all the claim for a given point in the fabric
        List<List<List<String>>> fabric = new ArrayList<>();
        // Create a list of claims
        List<Claim> claims = new ArrayList<>();

        // Initialize the list of list of list
        for (int i = 0; i < 1000; i++) {
            fabric.add(new ArrayList<>());
            for (int j = 0; j < 1000; j++) {
                fabric.get(i).add(new ArrayList<>());
            }
        }

        // Process each input in the input list
        // each input correspond to one claim
        inputs.forEach(input -> {
            // Split the input on the "@"
            // #1 @ 1,3: 4x4
            //    ↥
            // "#1 @ 1,3: 4x4" -> ["#1 ", " 1,3: 4x4"]
            String[] splitInput = input.split("@");

            // Get the id by taking only the first part of the splitInput
            // trimming it and removing the first "#"
            // ["#1 ", " 1,3: 4x4"] -> "#1 " -> "#1" -> "1"
            //         [0]          -> trim  -> substring
            String id = splitInput[0].trim().substring(1);

            // Split on the semi-colon to separate the position and the dimensions
            // " 1,3: 4x4" -> [" 1,3", " 4x4"]
            String[] nextParts = splitInput[1].split(":");
            // Split the first part on the comma to get the positions
            // " 1,3" -> [" 1", "3"]
            String[] offsets = nextParts[0].split(",");
            // Split the second part on the x to get the dimensions
            // " 4x4" -> [" 4", "4"]
            String[] dimensions = nextParts[1].split("x");

            // We need to trim because of the spaces before the number
            // [" 1,3", " 4x4"]
            //   ↥       ↥

            // Trim and parse the positions
            int leftOffset = Integer.parseInt(offsets[0].trim());
            int topOffset = Integer.parseInt(offsets[1].trim());

            // Trim and parse the dimensions
            int width = Integer.parseInt(dimensions[0].trim());
            int height = Integer.parseInt(dimensions[1].trim());

            // Add the claim to the list of claims
            claims.add(
                new Claim(
                    id,
                    leftOffset,
                    topOffset,
                    width,
                    height
                )
            );

            // Put the claim on the fabric
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    fabric.get(leftOffset + i).get(topOffset + j).add(id);
                }
            }
        });

        return new ParseContext(fabric, claims);
    }
}
