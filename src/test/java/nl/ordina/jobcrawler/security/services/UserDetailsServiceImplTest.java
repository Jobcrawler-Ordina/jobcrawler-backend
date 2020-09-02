package nl.ordina.jobcrawler.security.services;

import nl.ordina.jobcrawler.model.Role;
import nl.ordina.jobcrawler.model.RoleName;
import nl.ordina.jobcrawler.model.User;
import nl.ordina.jobcrawler.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserService userService;

    @Test
    public void loadByUserName() {
        User user = new User("admin", "password");
        user.setId(1L);
        Role adminRole = new Role();
        adminRole.setName(RoleName.ROLE_ADMIN);
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        user.setRoles(roles);

        when(userService.findByUsername("admin")).thenReturn(Optional.of(user));
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertEquals(userDetails.getUsername(), user.getUsername());
        verify(userService, times(1)).findByUsername("admin");
    }

    @Test
    public void loadByUserName_throws_exception() {
        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            when(userService.findByUsername("admin")).thenReturn(Optional.empty());
            UserDetails userDetails = userDetailsService.loadUserByUsername("admin");
        });
    }
}
