package nl.ordina.jobcrawler.payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
public class JwtResponse {
    private final String token;
    private final int expiresIn;
    private final String tokenType;
    private final String username;
    private final List<String> roles;
}
