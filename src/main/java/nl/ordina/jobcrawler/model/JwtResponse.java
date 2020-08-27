package nl.ordina.jobcrawler.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class JwtResponse {
    private String token;
    private int expiresIn;
    private String tokenType;
    private String username;
    private List<String> roles;
}
