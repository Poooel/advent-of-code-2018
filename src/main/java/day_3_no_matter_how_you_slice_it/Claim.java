package day_3_no_matter_how_you_slice_it;

import lombok.Value;

@Value
public class Claim {
    private String id;
    private int leftOffset;
    private int topOffset;
    private int width;
    private int height;
}
