package nl.ordina.jobcrawler.controller;

import nl.ordina.jobcrawler.model.Role;
import nl.ordina.jobcrawler.model.RoleName;
import nl.ordina.jobcrawler.model.User;
import nl.ordina.jobcrawler.model.UserDTO;
import nl.ordina.jobcrawler.service.RoleService;
import nl.ordina.jobcrawler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(path = "/user")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getUsers() {
        return userService.findAll();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(@Valid @RequestBody UserDTO userDTO) {
        User user = userService.convertToUser(userDTO);
        userService.update(user.getId(), user);
        return "OK";
    }

}
