package nl.ordina.jobcrawler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import nl.ordina.jobcrawler.model.Role;
import nl.ordina.jobcrawler.model.RoleName;
import nl.ordina.jobcrawler.model.User;
import nl.ordina.jobcrawler.model.UserForm;
import nl.ordina.jobcrawler.security.jwt.JwtProvider;
import nl.ordina.jobcrawler.service.RoleService;
import nl.ordina.jobcrawler.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ContextConfiguration(classes = { AuthController.class, TestSecurityConfiguration.class })
@WebMvcTest
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    @MockBean
    private RoleService roleService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtProvider jwtProvider;

    @Value("${jwt.token}") //jwt.token in application.properties
    private String jwtSecret;

    @Value("${jwt.expire}") // jwt.expire in application.properties
    private int jwtExpiration;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void POST_signin_with_valid_credentials() throws Exception {
        final String username = "admin";
        final String password = "password";

        UserForm userForm = new UserForm();
        userForm.setUsername(username);
        userForm.setPassword(password);

        User user = new User(username, password);
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(RoleName.ROLE_ADMIN);
        roles.add(role);
        user.setRoles(roles);


        when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(roleService.findByName(any())).thenReturn(Optional.of(role));
        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(userForm.getUsername(), null, authorityList));
        when(jwtProvider.generateJwtToken(any())).thenReturn(generateJwtToken(userForm.getUsername()));

        MvcResult result = mockMvc.perform(post("/auth/signin")
                .content(mapper.writeValueAsString(userForm))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        System.out.println(result.getResponse().getStatus());
        System.out.println(result.getResponse().getErrorMessage());
        System.out.println(result.getResponse().getContentAsString());

        assertEquals(result.getResponse().getStatus(), 200);
    }

    @Test
    public void GET_signup_allowance() throws Exception {
        mockMvc.perform(get("/auth/allow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allow").value(true));
    }

    private List<String> generateJwtToken(String username) {
        List<String> jwtToken = new ArrayList<>();
        jwtToken.add(Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact());
        jwtToken.add(String.valueOf(jwtExpiration));

        return jwtToken;
    }

}
