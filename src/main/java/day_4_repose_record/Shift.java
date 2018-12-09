package day_4_repose_record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shift {
    private List<Boolean> asleepMinutes;
    private String date;
}
