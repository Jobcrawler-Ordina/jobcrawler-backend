package nl.ordina.jobcrawler.controller;

import nl.ordina.jobcrawler.model.User;
import nl.ordina.jobcrawler.model.UserDTO;
import nl.ordina.jobcrawler.service.RoleService;
import nl.ordina.jobcrawler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<UserDTO> getUsers() {
        return userService.findAll()
                .stream()
                .map(userService::convertToUserDTO)
                .collect(Collectors.toList());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(@Valid @RequestBody UserDTO userDTO) {
        User user = userService.convertToUser(userDTO);
        userService.update(user.getId(), user);
        return "OK";
    }

    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity deleteUser(@PathVariable long id) {
        userService.delete(id);
        return ResponseEntity.ok("User removed");
    }
}
