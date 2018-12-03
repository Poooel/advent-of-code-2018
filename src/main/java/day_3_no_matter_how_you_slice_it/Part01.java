package day_3_no_matter_how_you_slice_it;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Part01
{
    public static void main(String [ ] args) throws IOException
    {
        // Get all the line in the file, as a list of string split on new line separator
        List<String> inputs = Files.readAllLines(Paths.get("data/day_03.input"));
    }
}
