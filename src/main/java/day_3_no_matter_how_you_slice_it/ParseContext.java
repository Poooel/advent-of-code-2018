package day_3_no_matter_how_you_slice_it;

import lombok.Value;

import java.util.List;

@Value
public class ParseContext {
    private List<List<List<String>>> fabric;
    private List<Claim> claims;
}
