package nl.ordina.jobcrawler.controller;

import nl.ordina.jobcrawler.model.User;
import nl.ordina.jobcrawler.model.UserDTO;
import nl.ordina.jobcrawler.service.RoleService;
import nl.ordina.jobcrawler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller contains user endpoints
 * /user for GETting all users and PUTting for updating a user
 * /user/{id} to DELETE a user
 */

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

    /**
     * Retrieves all users
     * @return all users converted to UserDTO
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> getUsers() {
        return userService.findAll()
                .stream()
                .map(userService::convertToUserDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update users with given details
     * @param userDTO Body needed to process this request
     * @return success message when succeeded updating user
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateUser(@Valid @RequestBody UserDTO userDTO) {
        User user = userService.convertToUser(userDTO);
        userService.update(user.getId(), user);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
    }

    /**
     * Delete user
     * @param id of user to be deleted
     * @return success message when succeeded deleting user
     */
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteUser(@PathVariable long id) {
        userService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "success", true,
                "message", "User removed"));
    }
}
