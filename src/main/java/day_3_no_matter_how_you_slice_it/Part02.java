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

        // parse the input exactly like part 1
        ParseContext parseContext = Part01.parseInput(inputs);

        // We will search for the id of the claim that doesn't overlap other
        // So we iterate each claim
        parseContext.getClaims().forEach(claim -> {
            // create a flag to abort the loop when haven't found the right claim
            boolean abort = false;

            // Walk the fabric space that the claim claimed
            for (int i = 0; i < claim.getWidth(); i++) {
                for (int j = 0; j < claim.getHeight(); j++) {
                    // If one of the claim point is consist of more than one claim (ours) then abort
                    // execution of the loop because this can't be this claim
                    if (parseContext.getFabric().get(claim.getLeftOffset() + i).get(claim.getTopOffset() + j).size() > 1) {
                        abort = true;
                        break;
                    }
                }
                if (abort) {
                    // flag to abort the outer loop
                    break;
                }
            }

            // If we didn't abort, it means we found one claim with every claim space with only
            // itself, so print its id
            if (!abort) {
                System.out.println(String.format("The answer is:%s", claim.getId()));
            }
        });
    }
}
