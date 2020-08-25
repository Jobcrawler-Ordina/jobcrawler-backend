package nl.ordina.jobcrawler.controller;

import nl.ordina.jobcrawler.controller.exception.UserNotFoundException;
import nl.ordina.jobcrawler.message.request.LoginForm;
import nl.ordina.jobcrawler.message.request.SignUpForm;
import nl.ordina.jobcrawler.message.response.JwtResponse;
import nl.ordina.jobcrawler.model.Role;
import nl.ordina.jobcrawler.model.RoleName;
import nl.ordina.jobcrawler.model.User;
import nl.ordina.jobcrawler.security.jwt.JwtProvider;
import nl.ordina.jobcrawler.service.RoleService;
import nl.ordina.jobcrawler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private boolean allowRegistration = true;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserService userService, RoleService roleService, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @GetMapping("/allow")
    public ResponseEntity<Object> allowRegistration() {
        return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("allow", this.allowRegistration));
    }

    @PutMapping("/allow")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateRegistration(@RequestParam(value = "newVal") boolean allow) {
        this.allowRegistration = allow;
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "success", true,
                "allow", this.allowRegistration));
    }

    @PostMapping("/signin")
    public ResponseEntity<Object> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        User user = userService.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UserNotFoundException(String.format("Fail! -> User with username %s not found!", loginRequest.getUsername())));
        Role adminRole = roleService.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Fail! -> Could not find admin role."));


        if (!user.getRoles().contains(adminRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "You don't have admin access."));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        List<String> jwt = jwtProvider.generateJwtToken(authentication);
        JwtResponse response = buildResponse(jwt, loginRequest.getUsername());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> registerUser(@Valid @RequestBody SignUpForm signUpRequest) {
        if (this.allowRegistration) {
            if (userService.existsByUsername(signUpRequest.getUsername())) {
                return new ResponseEntity<>("Fail -> Username is already taken!",
                        HttpStatus.BAD_REQUEST);
            }

            User user = new User(signUpRequest.getUsername(), passwordEncoder.encode(signUpRequest.getPassword()));

            Set<Role> roles = new HashSet<>();
            if (userService.count() == 0) {
                Role adminRole = roleService.findByName(RoleName.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Fail! -> Could not find admin role."));
                roles.add(adminRole);
            }

            Role userRole = roleService.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Fail! -> Could not find user role."));
            roles.add(userRole);

            user.setRoles(roles);
            userService.save(user);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "User registered successfully!"));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "Registration is forbidden"));
        }
    }

    @GetMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JwtResponse> refreshToken(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String bearerStr) {
        String tokenStr = bearerStr.split("Bearer ")[1];
        List<String> jwt = jwtProvider.refreshToken(tokenStr);
        String username = jwtProvider.getUserNameFromJwtToken(tokenStr);

        JwtResponse response = buildResponse(jwt, username);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private JwtResponse buildResponse(List<String> jwt, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(String.format("Fail! -> User with name: %s not found.", username)));


        List<String> roles = new ArrayList<>();
        user.getRoles().forEach(role -> {
            roles.add(String.valueOf(role.getName()));
        });

        return JwtResponse.builder()
                .token(jwt.get(0))
                .expiresIn(Integer.parseInt(jwt.get(1)))
                .tokenType("BEARER")
                .username(username)
                .roles(roles)
                .build();
    }
}
