package nl.ordina.jobcrawler.security.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.security.services.UserPrincipal;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class JwtProvider {

    private static final String JWT_SECRET = "BE43B886805D7AAC35A6B7B4E8EC2A95A12067BDD6D0D51E9456AF14C78B8217";

    private static final int JWT_EXPIRATION = 1800;

    /**
     * Generates JWT Token based on user credentials
     * @param authentication authentication based on user credentials
     * @return List including jwt Token and jwt expiration information
     */
    public List<String> generateJwtToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        List<String> jwtToken = new ArrayList<>();
        jwtToken.add(Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION * 1000))
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact());
        jwtToken.add(String.valueOf(JWT_EXPIRATION));

        return jwtToken;
    }

    /**
     * Retrieve username based on jwt token
     * @param token token username is needed for
     * @return username
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    /**
     * Validates validity of token
     * @param authToken token
     * @return if token is valid
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            final String errorMessage = "Unable to validate the access token.";
            log.error(errorMessage + " -> Message: {} ", e.getMessage());
            throw new JwtException(errorMessage, e);
        }

    }

    /**
     * Refresh jwt token based on current (valid) token
     * @param authToken current working token
     * @return new token
     */
    @SuppressWarnings("java:S2583")
    /**
     *    SonarLint reports this issue on the line: if (claimsJws.isEmpty()) below; it is a false positive.
     */
    public List<String> refreshToken(String authToken) {
        validateJwtToken(authToken);
        Optional<Jws<Claims>> claimsJws = getClaims(Optional.of(authToken));
        if (claimsJws.isEmpty()) {
            throw new AuthorizationServiceException("Invalid token claims");
        }
        Claims claims = claimsJws.get().getBody();
        claims.setIssuedAt(new Date());
        claims.setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION * 1000));
        List<String> refreshToken = new ArrayList<>();
        refreshToken.add(Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, JWT_SECRET).compact());
        refreshToken.add(String.valueOf(JWT_EXPIRATION));

        return refreshToken;
    }

    private Optional<Jws<Claims>> getClaims(Optional<String> authToken) {
        if (authToken.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(authToken.get()));
    }
}
