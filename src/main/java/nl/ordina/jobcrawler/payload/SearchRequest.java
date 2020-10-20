package nl.ordina.jobcrawler.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

// on the server jackson gives an error that this class does not have a default constructor, which cannot exist with a final variable
//@Value  // makes getters and makes attributes private final
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SearchRequest {

    private String distance;
    private String keywords;
    private Set<String> skills;

    private LocalDateTime fromDate;
    private LocalDateTime toDate;

}
