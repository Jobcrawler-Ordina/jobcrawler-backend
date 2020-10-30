package nl.ordina.jobcrawler.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import nl.ordina.jobcrawler.security.services.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtProviderTest {

    private final JwtProvider jwtProvider = new JwtProvider();

    private Authentication authentication;
    private String token;

    @BeforeEach
    public void init() {
        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        UserPrincipal userPrinciple = new UserPrincipal(1L, "admin", "password", authorityList);
        authentication = new UsernamePasswordAuthenticationToken(userPrinciple, null, authorityList);
        token = jwtProvider.generateJwtToken(authentication).get(0);
    }

    @Test
    void generateJwtTokenTest() {
        List<String> jwt = jwtProvider.generateJwtToken(authentication);
        assertNotNull(jwt.get(0));
        assertEquals("1800", jwt.get(1));
    }

    @Test
    void getUserNameFromJwtTokenTest() {
        String username = jwtProvider.getUserNameFromJwtToken(token);
        assertEquals("admin", username);
    }

    @Test
    void validateJwtTokenTest() {
        boolean valid = jwtProvider.validateJwtToken(token);
        assertTrue(valid);
    }

    @Test
    void validateJwtTokenTest_invalid_token_MalformedJwtException() {
        assertThatThrownBy(() -> {
            boolean valid = jwtProvider.validateJwtToken("randomstringthatshouldactasatoken");
        }).isInstanceOf(JwtException.class)
            .hasCauseInstanceOf(MalformedJwtException.class);
    }

    @Test
    void validateJwtTokenTest_empty_token_IllegalArgumentException() {
        assertThatThrownBy(() -> {
            boolean valid = jwtProvider.validateJwtToken("");
        }).isInstanceOf(JwtException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateJwtTokenTest_expired_token_ExpiredJwtException() {
        assertThatThrownBy(() -> {
            boolean valid = jwtProvider.validateJwtToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aW1vIiwiaWF0IjoxNTk5MDMyODg5LCJleHAiOjE1OTkwMzQ2ODl9.-ULgqcWxzyljTxSkJspLPW9-kHkRSt695PLa9MnGR-hT70X6DVQaGV58UHvc5rTmXt3OtgFpAAjNbhqkqfESrw");
        }).isInstanceOf(JwtException.class)
                .hasCauseInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void refreshTokenTest() {
        List<String> newToken = jwtProvider.refreshToken(token);
        assertEquals(2, newToken.size());
        assertNotNull(newToken.get(0));
        assertEquals("1800", newToken.get(1));
    }

    @Test
    void refreshToken_throws_exception() {
        assertThatThrownBy(() -> {
            List<String> newToken = jwtProvider.refreshToken("");
        }).isInstanceOf(JwtException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }
}
