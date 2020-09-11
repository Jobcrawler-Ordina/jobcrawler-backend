package nl.ordina.jobcrawler.service;

import nl.ordina.jobcrawler.controller.exception.UserNotFoundException;
import nl.ordina.jobcrawler.model.Role;
import nl.ordina.jobcrawler.model.RoleName;
import nl.ordina.jobcrawler.model.User;
import nl.ordina.jobcrawler.model.UserDTO;
import nl.ordina.jobcrawler.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

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
    public void findByID() {
        int id = 5;
        when(userRepository.findById((long) id)).thenReturn(Optional.of(userList.get(id - 1)));
        User user = userService.findById((long) id)
                .orElseThrow(() -> new UserNotFoundException(String.format("Fail! -> User with id %d not found!", id)));

        assertEquals(user.getId(), id);
        verify(userRepository, times(1)).findById((long) id);
    }

    @Test
    public void findAll() {
        when(userRepository.findAll()).thenReturn(userList);
        List<User> allUsers = userService.findAll();

        assertEquals(allUsers.size(), userList.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void updateUser() {
        User user4 = userList.get(4);
        user4.setUsername("newUsername");
        when(userRepository.save(user4)).thenReturn(user4);
        User updatedUser = userService.update(user4.getId(), user4);

        assertEquals(updatedUser.getUsername(), user4.getUsername());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void updateDitIsAlGenoeg() {
        userService.update(1L, new User());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    public void saveUser() {
        User newUser = new User("newUser", "newPassword");
        newUser.setId(11L);
        when(userRepository.save(newUser)).thenReturn(newUser);
        User savedUser = userService.save(newUser);

        assertEquals(savedUser.getUsername(), newUser.getUsername());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    public void deleteUser() {
        long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.of(userList.get((int) id - 1)));
        doNothing().when(userRepository).delete(any());
        boolean result = userService.delete(id);

        assertTrue(result);
        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).delete(any());
    }

    @Test
    public void findByIdAndUsername() {
        long id = 6L;
        User user6 = userList.get((int) id);
        when(userRepository.findByIdAndUsername(id, user6.getUsername())).thenReturn(Optional.of(user6));

        User user = userService.findByIdAndUsername(id, user6.getUsername())
                .orElseThrow(() -> new UserNotFoundException(String.format("Fail! -> User with id %d not found!", id)));

        assertEquals(user.getUsername(), user6.getUsername());
        verify(userRepository, times(1)).findByIdAndUsername(id, user6.getUsername());
    }

    @Test
    public void findByUsername() {
        User user7 = userList.get(7);
        when(userRepository.findByUsername(user7.getUsername())).thenReturn(Optional.of(user7));

        User user = userService.findByUsername(user7.getUsername())
                .orElseThrow(() -> new UserNotFoundException(String.format("Fail -> User with username %s not found!", user7.getUsername())));

        assertEquals(user.getUsername(), user7.getUsername());
        verify(userRepository, times(1)).findByUsername(user7.getUsername());
    }

    @Test
    public void count() {
        when(userRepository.count()).thenReturn((long) userList.size());
        long count = userService.count();

        assertEquals(count, userList.size());
        verify(userRepository, times(1)).count();
    }

    @Test
    public void existByUsername() {
        User user8 = userList.get(8);
        when(userRepository.existsByUsername(user8.getUsername())).thenReturn(true);

        boolean value = userService.existsByUsername(user8.getUsername());

        assertTrue(value);
        verify(userRepository, times(1)).existsByUsername(user8.getUsername());
    }

    @Test
    public void convertToUser() {
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

        assertEquals(convertedUser.getRoles().size(), 2);
        assertEquals(convertedUser.getUsername(), userDTO.getUsername());
        assertTrue(convertedUser.getRoles().contains(adminRole));

        verify(userRepository, times(1)).findByIdAndUsername(userDTO.getId(), userDTO.getUsername());
        verify(roleService, times(2)).findByName(any());
    }

    @Test
    public void convertToUserDTO() {
        User user9 = userList.get(9);
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(userRole);
        user9.setRoles(roles);

        UserDTO userDTO = userService.convertToUserDTO(user9);

        assertEquals(userDTO.getUsername(), user9.getUsername());
        assertEquals(userDTO.getRoles().size(), 2);
        assertTrue(userDTO.getRoles().contains("ROLE_ADMIN"));
        assertTrue(userDTO.getRoles().contains("ROLE_USER"));
    }

}
