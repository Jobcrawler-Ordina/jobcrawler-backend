package nl.ordina.jobcrawler.message.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
    private String token;
    private String tokenType = "Bearer";

    public JwtResponse(String accessToken) {
        this.token = accessToken;
    }
}
