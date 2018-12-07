package day_4_repose_record;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class Part01
{
    public static void main(String[] args) throws Exception
    {
        // Get all the line in the file, as a list of string split on new line separator
        List<String> inputs = Files.readAllLines(Paths.get("input/day_04.input"));

        Collections.sort(inputs);

        
    }
}
