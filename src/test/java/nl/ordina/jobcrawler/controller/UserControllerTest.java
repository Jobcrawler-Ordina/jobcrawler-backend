package nl.ordina.jobcrawler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ordina.jobcrawler.model.Role;
import nl.ordina.jobcrawler.util.RoleName;
import nl.ordina.jobcrawler.model.User;
import nl.ordina.jobcrawler.util.UserDTO;
import nl.ordina.jobcrawler.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ContextConfiguration(classes = { UserController.class, TestSecurityConfiguration.class })
@WebMvcTest
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    private List<User> userList = new ArrayList<>();
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    public void init() {
        User user = new User("admin", "password");
        user.setId(1L);
        adminRole = new Role();
        adminRole.setName(RoleName.ROLE_ADMIN);
        Set<Role> roleAdmin = new HashSet<>();
        roleAdmin.add(adminRole);
        user.setRoles(roleAdmin);

        User user1 = new User("user", "password");
        user1.setId(2L);
        userRole = new Role();
        userRole.setName(RoleName.ROLE_USER);
        Set<Role> roleUser = new HashSet<>();
        roleUser.add(userRole);
        user.setRoles(roleUser);

        userList.add(user);
        userList.add(user1);
    }

    @Test
    void GET_all_users() throws Exception {
        when(userService.findAll()).thenReturn(userList);
        when(userService.convertToUserDTO(any())).thenCallRealMethod();

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(userService, times(1)).findAll();
        verify(userService, times(2)).convertToUserDTO(any());
    }

    @Test
    void PUT_update_user() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userList.get(0).getId());
        userDTO.setUsername(userList.get(0).getUsername());
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        roles.add("ROLE_ADMIN");
        userDTO.setRoles(roles);

        User user = userList.get(0);
        Set<Role> newRoles = new HashSet<>();
        newRoles.add(userRole);
        newRoles.add(adminRole);
        user.setRoles(newRoles);

        when(userService.convertToUser(any())).thenReturn(user);
        when(userService.update(any())).thenReturn(null);

        mockMvc.perform(put("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(userService, times(1)).convertToUser(any());
        verify(userService, times(1)).update(any());
    }

    @Test
    void DELETE_user_by_id() throws Exception {
        when(userService.delete(anyLong())).thenReturn(true);
        mockMvc.perform(delete("/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(userService, times(1)).delete(anyLong());
    }


}
