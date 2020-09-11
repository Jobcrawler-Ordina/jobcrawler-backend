package nl.ordina.jobcrawler.service;

import nl.ordina.jobcrawler.exception.RoleNotFoundException;
import nl.ordina.jobcrawler.model.Role;
import nl.ordina.jobcrawler.util.RoleName;
import nl.ordina.jobcrawler.repo.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role adminRole;
    private Role userRole;
    private List<Role> roleList;

    @BeforeEach
    public void init() {
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName(RoleName.ROLE_ADMIN);

        userRole = new Role();
        userRole.setId(2L);
        userRole.setName(RoleName.ROLE_USER);

        roleList = new ArrayList<>();
        roleList.add(adminRole);
        roleList.add(userRole);
    }

    @Test
    public void findAll() {
        when(roleRepository.findAll()).thenReturn(roleList);
        List<Role> findAllRoles = roleService.findAll();

        assertNotNull(findAllRoles);
        assertEquals(findAllRoles.size(), 2);
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    public void save() {
        when(roleRepository.save(userRole)).thenReturn(userRole);
        Role savedRole = roleService.save(userRole);

        assertNotNull(savedRole);
        assertEquals(savedRole.getName(), userRole.getName());
        verify(roleRepository, times(1)).save(userRole);
    }

    @Test
    public void delete() {
        when(roleRepository.findById(adminRole.getId())).thenReturn(Optional.of(adminRole));
        doNothing().when(roleRepository).delete(any());

        boolean result = roleService.delete(adminRole.getId());
        assertTrue(result);
        verify(roleRepository, times(1)).findById(adminRole.getId());
        verify(roleRepository, times(1)).delete(any());
    }

    @Test
    public void findByName() {
        when(roleRepository.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        Role role = roleService.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        assertEquals(role.getName(), adminRole.getName());
        verify(roleRepository, times(1)).findByName(RoleName.ROLE_ADMIN);
    }
}
