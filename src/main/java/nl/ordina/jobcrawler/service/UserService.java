package nl.ordina.jobcrawler.service;

import nl.ordina.jobcrawler.controller.exception.RoleNotFoundException;
import nl.ordina.jobcrawler.controller.exception.UserNotFoundException;
import nl.ordina.jobcrawler.model.Role;
import nl.ordina.jobcrawler.model.RoleName;
import nl.ordina.jobcrawler.model.User;
import nl.ordina.jobcrawler.model.UserDTO;
import nl.ordina.jobcrawler.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements CRUDService<User, Long> {

    private final UserRepository userRepository;
    private final RoleService roleService;

    @Autowired
    public UserService(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // TODO Long aLong is not used can be removed
    @Override
    public User update(Long aLong, User user) {
        return userRepository.save(user);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("Fail! -> Could not find user with id: %d in database.", id)));
        userRepository.delete(user);
        return true;
    }

    public Optional<User> findByIdAndUsername(long id, String username) {
        return userRepository.findByIdAndUsername(id, username);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public long count() {
        return userRepository.count();
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User convertToUser(UserDTO userDTO) {
        User user = findByIdAndUsername(userDTO.getId(), userDTO.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Fail! -> Could not find user in database."));

        Set<Role> newRoles = new HashSet<>();

        userDTO.getRoles().forEach(role -> {
        Role addRole = roleService.findByName(RoleName.valueOf(role))
              .orElseThrow(() -> new RoleNotFoundException("Fail! -> Could not find: " + role + " in database."));
        newRoles.add(addRole);
        });

        user.setRoles(newRoles);

        return user;
    }

    public UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        user.getRoles().forEach(role -> userDTO.addRole(String.valueOf(role.getName())));
        return userDTO;
    }
}
