package nl.ordina.jobcrawler.scrapers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Map;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HuxleyITResponse {

    private Integer hits;
    private ArrayList<Map<String, Object>> vacanciesData;

    /**
        This annotation indicates to Jackson that something must be executed upon the "result" parameter of the returned JSON.

        This method extracts the needed vacancy data from the "result" data and stores it as List of key-value pairs to return.

        An interesting point is that Jackson converts the JSON objects to Map<String, Object>, where the keys are the parameter names.

        The JSON that Jackson has parsed has this format (only the parameters used by us are noted here):
        {
            "result": {
                "hits": 60,
                "results": [
                    {
                        "jobReference": "HA-XXXXXXXX_XXXXXXXXXX",
                        "title": "XXXXXX",
                        "city": "XXXXXX",
                        "salaryText": "XXXXXX",
                        "postDate": "2020-04-14T07:41:38.0679209Z",
                        "description": "XXXXXX"
                    }
                ]
            }
        }
     */
    @JsonProperty("result")
    private void unpackNested(Map<String, Object> result) {
        this.hits = (Integer) result.get("hits");

        this.vacanciesData = (ArrayList<Map<String, Object>>) result.get("results");
    }
}
