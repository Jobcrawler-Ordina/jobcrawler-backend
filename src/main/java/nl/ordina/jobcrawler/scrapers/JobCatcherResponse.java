package nl.ordina.jobcrawler.scrapers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class JobCatcherResponse {

    @JsonProperty("Data")
    private ArrayList<JobCatcherResponseData> data;
    @JsonProperty("Errors")
    private List<String> errors;
    @JsonProperty("ResultCode")
    private String resultCode;

}
