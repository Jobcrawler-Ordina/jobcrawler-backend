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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { AuthController.class })
@WebMvcTest
@WithMockUser
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

    private Role userRole;

    private Role adminRole;

    private UserForm userForm;

    private User user;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        userRole = new Role();
        userRole.setName(RoleName.ROLE_USER);

        adminRole = new Role();
        adminRole.setName(RoleName.ROLE_ADMIN);

        userForm = new UserForm();
        userForm.setUsername("admin");
        userForm.setPassword("password");

        user = new User(userForm.getUsername(), userForm.getPassword());

        when(roleService.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(userForm.getUsername(), null, authorityList));
        when(jwtProvider.generateJwtToken(any())).thenReturn(generateJwtToken(userForm.getUsername()));
        when(userService.save(any())).thenReturn(null);
    }

    @Test
    public void POST_signin_with_valid_credentials() throws Exception {
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        user.setRoles(roles);

        when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(post("/auth/signin")
                .with(csrf())
                .content(mapper.writeValueAsString(userForm))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value(userForm.getUsername()));
    }

    @Test
    public void POST_signin_no_admin_role() throws Exception {
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(post("/auth/signin")
                .with(csrf())
                .content(mapper.writeValueAsString(userForm))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You don't have admin access."));
    }

    @Test
    public void GET_allowance() throws Exception {
        setAllowance(true);

        mockMvc.perform(get("/auth/allow")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allow").value(true));
    }

    @Test
    public void PUT_change_allowance() throws Exception {
        mockMvc.perform(get("/auth/allow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allow").value(true));

        mockMvc.perform(put("/auth/allow?newVal=false")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allow").value(false))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void POST_signup_success() throws Exception {
        setAllowance(true);
        when(userService.existsByUsername(anyString())).thenReturn(false);
        when(userService.count()).thenReturn(0L);
        when(roleService.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));

        mockMvc.perform(post("/auth/signup")
                .with(csrf())
                .content(mapper.writeValueAsString(userForm))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }

    @Test
    public void POST_signup_username_taken() throws Exception {
        setAllowance(true);
        when(userService.existsByUsername(anyString())).thenReturn(true);

        mockMvc.perform(post("/auth/signup")
                .with(csrf())
                .content(mapper.writeValueAsString(userForm))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Fail -> Username is already taken!"));
    }

    @Test
    public void POST_signup_registration_closed() throws Exception {
        setAllowance(false);

        mockMvc.perform(post("/auth/signup")
                .with(csrf())
                .content(mapper.writeValueAsString(userForm))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Registration is forbidden"));
    }

    @Test
    public void GET_refresh_token() throws Exception {
        when(jwtProvider.refreshToken(anyString())).thenReturn(generateJwtToken(userForm.getUsername()));
        when(jwtProvider.getUserNameFromJwtToken(anyString())).thenReturn(userForm.getUsername());
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/auth/refresh")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
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

    private void setAllowance(boolean val) throws Exception {
        mockMvc.perform(put("/auth/allow?newVal=" + val)
                .with(csrf()));
    }

}
