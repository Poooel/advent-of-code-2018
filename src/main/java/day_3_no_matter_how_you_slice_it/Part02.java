package day_3_no_matter_how_you_slice_it;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Part02
{
    public static void main(String[] args) throws IOException
    {
        // Get all the line in the file, as a list of string split on new line separator
        List<String> inputs = Files.readAllLines(Paths.get("input/day_03.input"));

        ParseContext parseContext = Part01.parseInput(inputs);

        parseContext.getClaims().forEach(claim -> {
            boolean abort = false;

            for (int i = 0; i < claim.getWidth(); i++) {
                for (int j = 0; j < claim.getHeight(); j++) {
                    if (parseContext.getFabric().get(claim.getLeftOffset() + i).get(claim.getTopOffset() + j).size() > 1) {
                        abort = true;
                        break;
                    }
                }
                if (abort) {
                    break;
                }
            }

            if (!abort) {
                System.out.println(String.format("The answer is:%s", claim.getId()));
            }
        });
    }
}
