package nl.ordina.jobcrawler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ordina.jobcrawler.exception.RoleNotFoundException;
import nl.ordina.jobcrawler.exception.UserNotFoundException;
import nl.ordina.jobcrawler.model.Role;
import nl.ordina.jobcrawler.util.RoleName;
import nl.ordina.jobcrawler.model.User;
import nl.ordina.jobcrawler.payload.UserRequest;
import nl.ordina.jobcrawler.security.jwt.JwtProvider;
import nl.ordina.jobcrawler.security.services.UserPrinciple;
import nl.ordina.jobcrawler.service.RoleService;
import nl.ordina.jobcrawler.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ContextConfiguration(classes = { AuthController.class })
@WebMvcTest
@WithMockUser
class AuthControllerTest {

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

    private Role userRole;

    private Role adminRole;

    private UserRequest userForm;

    private User user;

    private UserPrinciple userPrinciple;

    private List<GrantedAuthority> authorityList;

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

        userForm = new UserRequest();
        userForm.setUsername("admin");
        userForm.setPassword("password");

        user = new User(userForm.getUsername(), userForm.getPassword());

        when(roleService.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        userPrinciple = new UserPrinciple(1L, userForm.getUsername(), userForm.getPassword(), authorityList);
        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(userPrinciple, null, authorityList));
        when(jwtProvider.generateJwtToken(any())).thenCallRealMethod();
        when(userService.save(any())).thenReturn(null);
    }

    @Test
    void POST_signin_with_valid_credentials() throws Exception {
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

        verify(authenticationManager, times(1)).authenticate(any());
        verify(userService, times(2)).findByUsername(anyString());
        verify(roleService, times(1)).findByName(RoleName.ROLE_ADMIN);
        verify(jwtProvider, times(1)).generateJwtToken(any());
    }

    @Test
    void POST_signin_no_admin_role() throws Exception {
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

        verify(authenticationManager, times(1)).authenticate(any());
        verify(userService, times(1)).findByUsername(anyString());
        verify(roleService, times(1)).findByName(RoleName.ROLE_ADMIN);
    }

    @Test
    void POST_signin_throws_UserNotFoundException() throws Exception {
            when(userService.findByUsername(anyString())).thenReturn(Optional.empty());

            mockMvc.perform(post("/auth/signin")
                    .with(csrf())
                    .content(mapper.writeValueAsString(userForm))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));

        verify(authenticationManager, times(1)).authenticate(any());
        verify(userService, times(1)).findByUsername(anyString());
    }

    @Test
    void POST_signin_throws_RoleNotFoundException() throws Exception {
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(roleService.findByName(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/signin")
                .with(csrf())
                .content(mapper.writeValueAsString(userForm))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RoleNotFoundException));

        verify(authenticationManager, times(1)).authenticate(any());
        verify(userService, times(1)).findByUsername(anyString());
        verify(roleService, times(1)).findByName(RoleName.ROLE_ADMIN);
    }

    @Test
    void GET_allowance() throws Exception {
        setAllowance(true);

        mockMvc.perform(get("/auth/allow")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allow").value(true));
    }

    @Test
    void PUT_change_allowance() throws Exception {
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
    void POST_signup_success() throws Exception {
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

        verify(userService, times(1)).existsByUsername(anyString());
        verify(userService, times(1)).count();
        verify(roleService, times(2)).findByName(any());
        verify(userService, times(1)).save(any());
    }

    @Test
    void POST_signup_username_taken() throws Exception {
        setAllowance(true);
        when(userService.existsByUsername(anyString())).thenReturn(true);

        mockMvc.perform(post("/auth/signup")
                .with(csrf())
                .content(mapper.writeValueAsString(userForm))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Fail -> Username is already taken!"));

        verify(userService, times(1)).existsByUsername(anyString());
    }

    @Test
    void POST_signup_registration_closed() throws Exception {
        setAllowance(false);

        mockMvc.perform(post("/auth/signup")
                .with(csrf())
                .content(mapper.writeValueAsString(userForm))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Registration is forbidden"));
    }

    @Test
    void GET_refresh_token() throws Exception {
        // First generate valid token that needs to be refreshed. Can't refresh an invalid token.
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrinciple, null, authorityList);
        String jwtToken = jwtProvider.generateJwtToken(authentication).get(0);

        // Use real methods for refreshing token
        when(jwtProvider.refreshToken(anyString())).thenCallRealMethod();
        when(jwtProvider.getUserNameFromJwtToken(anyString())).thenCallRealMethod();
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/auth/refresh")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(userForm.getUsername()));

        verify(jwtProvider, times(1)).refreshToken(anyString());
        verify(jwtProvider, times(1)).getUserNameFromJwtToken(anyString());
        verify(userService, times(1)).findByUsername(anyString());
    }

    private void setAllowance(boolean val) throws Exception {
        mockMvc.perform(put("/auth/allow?newVal=" + val)
                .with(csrf()));
    }

}
