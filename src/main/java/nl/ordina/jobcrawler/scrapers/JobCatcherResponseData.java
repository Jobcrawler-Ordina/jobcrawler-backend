package nl.ordina.jobcrawler.scrapers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class JobCatcherResponseData {

    @JsonProperty("List")
    private List<Map<String,Object>> list;
    @JsonProperty("AllowedActions")
    private List<Object> allowedActions;
    @JsonProperty("Amount")
    private Integer amount;

}
