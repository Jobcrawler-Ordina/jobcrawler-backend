package nl.ordina.jobcrawler.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SearchRequest {

    private String keywords;
    private Set<String> skills;

    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    private Double distance;
    private String location;
    private double[] coord;

}
