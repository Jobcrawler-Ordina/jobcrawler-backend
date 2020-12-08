package nl.ordina.jobcrawler.payload.opensearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Place {

    @JsonProperty("place_id")
    private String placeId;
    private Address address;
    @JsonProperty("display_name")
    private String displayName;

}
