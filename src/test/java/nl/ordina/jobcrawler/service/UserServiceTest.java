package nl.ordina.jobcrawler.service;

import nl.ordina.jobcrawler.exception.UserNotFoundException;
import nl.ordina.jobcrawler.model.Role;
import nl.ordina.jobcrawler.util.RoleName;
import nl.ordina.jobcrawler.model.User;
import nl.ordina.jobcrawler.util.UserDTO;
import nl.ordina.jobcrawler.repo.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserService userService;

    private List<User> userList;

    private Role adminRole;

    private Role userRole;

    @BeforeEach
    public void init() {
        userList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int id = i + 1;
            User user = new User("username" + id, "password" + id);
            user.setId(id);
            userList.add(user);
        }

        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName(RoleName.ROLE_ADMIN);

        userRole = new Role();
        userRole.setId(2L);
        userRole.setName(RoleName.ROLE_USER);
    }

    @Test
    void findAll() {
        when(userRepository.findAll()).thenReturn(userList);
        List<User> allUsers = userService.findAll();

        assertEquals(userList.size(), allUsers.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void update() {
        userService.update(new User());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void saveUser() {
        userService.save(new User());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void deleteUser() {
        boolean result = userService.delete(1L);
        assertTrue(result);
        verify(userRepository, times(1)).deleteById(any());
    }

    @Test
    void deleteUser_throws_exception() {
        doThrow(EmptyResultDataAccessException.class).when(userRepository).deleteById(anyLong());
        Assertions.assertThrows(UserNotFoundException.class, () -> {
           userService.delete(1L);
        });
    }

    @Test
    void findByIdAndUsername() {
        long id = 6L;
        User user6 = userList.get((int) id);
        when(userRepository.findByIdAndUsername(id, user6.getUsername())).thenReturn(Optional.of(user6));

        User user = userService.findByIdAndUsername(id, user6.getUsername())
                .orElseThrow(() -> new UserNotFoundException(String.format("Fail! -> User with id %d not found!", id)));

        assertEquals(user.getUsername(), user6.getUsername());
        verify(userRepository, times(1)).findByIdAndUsername(id, user6.getUsername());
    }

    @Test
    void findByUsername() {
        User user7 = userList.get(7);
        when(userRepository.findByUsername(user7.getUsername())).thenReturn(Optional.of(user7));

        User user = userService.findByUsername(user7.getUsername())
                .orElseThrow(() -> new UserNotFoundException(String.format("Fail -> User with username %s not found!", user7.getUsername())));

        assertEquals(user.getUsername(), user7.getUsername());
        verify(userRepository, times(1)).findByUsername(user7.getUsername());
    }

    @Test
    void count() {
        when(userRepository.count()).thenReturn((long) userList.size());
        long count = userService.count();

        assertEquals(count, userList.size());
        verify(userRepository, times(1)).count();
    }

    @Test
    void existByUsername() {
        User user8 = userList.get(8);
        when(userRepository.existsByUsername(user8.getUsername())).thenReturn(true);

        boolean value = userService.existsByUsername(user8.getUsername());

        assertTrue(value);
        verify(userRepository, times(1)).existsByUsername(user8.getUsername());
    }

    @Test
    void convertToUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("username1");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        roles.add("ROLE_USER");
        userDTO.setRoles(roles);

        when(userService.findByIdAndUsername(userDTO.getId(), userDTO.getUsername())).thenReturn(Optional.of(userList.get(0)));
        when(roleService.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleService.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));

        User convertedUser = userService.convertToUser(userDTO);

        assertEquals(2, convertedUser.getRoles().size());
        assertEquals(userDTO.getUsername(), convertedUser.getUsername());
        assertTrue(convertedUser.getRoles().contains(adminRole));

        verify(userRepository, times(1)).findByIdAndUsername(userDTO.getId(), userDTO.getUsername());
        verify(roleService, times(2)).findByName(any());
    }

    @Test
    void convertToUserDTO() {
        User user9 = userList.get(9);
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(userRole);
        user9.setRoles(roles);

        UserDTO userDTO = userService.convertToUserDTO(user9);

        assertEquals(user9.getUsername(), userDTO.getUsername());
        assertEquals(2, userDTO.getRoles().size());
        assertTrue(userDTO.getRoles().contains("ROLE_ADMIN"));
        assertTrue(userDTO.getRoles().contains("ROLE_USER"));
    }

}
