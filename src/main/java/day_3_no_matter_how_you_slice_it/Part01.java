package day_3_no_matter_how_you_slice_it;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Part01
{
    public static void main(String[] args) throws IOException
    {
        System.out.println(String.format("The answer is: %s",
            parseInput(
                Files.readAllLines(
                    Paths.get("input/day_03.input")
                )
            )
                .getFabric()
                .stream()
                .map(Collection::stream)
                .flatMap(x -> x)
                .filter(x -> x.size() > 1)
                .count()
            )
        );
    }

    public static ParseContext parseInput(List<String> inputs) {
        List<List<List<String>>> fabric = new ArrayList<>();
        List<Claim> claims = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            fabric.add(new ArrayList<>());
            for (int j = 0; j < 1000; j++) {
                fabric.get(i).add(new ArrayList<>());
            }
        }

        inputs.forEach(input -> {
            String[] splitInput = input.split("@");

            String id = splitInput[0].trim().substring(1);

            String[] nextParts = splitInput[1].split(":");
            String[] offsets = nextParts[0].split(",");
            String[] dimensions = nextParts[1].split("x");

            int leftOffset = Integer.parseInt(offsets[0].trim());
            int topOffset = Integer.parseInt(offsets[1].trim());

            int width = Integer.parseInt(dimensions[0].trim());
            int height = Integer.parseInt(dimensions[1].trim());

            claims.add(
                new Claim(
                    id,
                    leftOffset,
                    topOffset,
                    width,
                    height
                )
            );

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    fabric.get(leftOffset + i).get(topOffset + j).add(id);
                }
            }
        });

        return new ParseContext(fabric, claims);
    }
}
