package day_4_repose_record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaximumContext {
    private int index;
    private int maximum;
    private int guardId;
}
