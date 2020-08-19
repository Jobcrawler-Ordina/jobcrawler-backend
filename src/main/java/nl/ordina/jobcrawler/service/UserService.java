package nl.ordina.jobcrawler.service;

import nl.ordina.jobcrawler.model.User;
import nl.ordina.jobcrawler.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements CRUDService<User, Long> {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User update(Long aLong, User user) {
        return null;
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    public long count() {
        return userRepository.count();
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
