package nl.ordina.jobcrawler.service;

import nl.ordina.jobcrawler.exception.SkillNotFoundException;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.repo.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {
    @Mock
    SkillRepository mockSkillRepository;

    @InjectMocks
    SkillService skillService;

    private List<Skill> skillList;

    @BeforeEach
    public void init() {
        skillList = createMockSkillList();
    }

    @Test
    void testFindByOrderByNameAsc() {
        when(mockSkillRepository.findByOrderByNameAsc()).thenReturn(skillList);
        List<Skill> allSkills = skillService.findByOrderByNameAsc();

        assertEquals(skillList.size(), allSkills.size());
        verify(mockSkillRepository, times(1)).findByOrderByNameAsc();
    }

    @Test
    void testFindById() {
        Skill skill = skillList.get(5);
        UUID uuid = skill.getId();
        when(mockSkillRepository.findById(uuid))
                .thenReturn(Optional.of(skill))
                .thenReturn(Optional.empty());

        // 1. test with Skill result
        Optional<Skill> foundSkill = skillService.findById(skill.getId());

        assertTrue(foundSkill.isPresent());
        assertEquals(skill, foundSkill.get());

        // 2. test with empty result
        foundSkill = skillService.findById(uuid);

        assertTrue(foundSkill.isEmpty());

        verify(mockSkillRepository, times(2)).findById(uuid);
    }

    @Test
    void testFindAll() {
        when(mockSkillRepository.findAll()).thenReturn(skillList);
        List<Skill> allSkills = skillService.findAll();

        assertEquals(skillList.size(), allSkills.size());
        verify(mockSkillRepository, times(1)).findAll();
    }

    @Test
    void testUpdate() {
        Skill skill = skillList.get(7);
        UUID uuid = skill.getId();
        Skill newSkill = new Skill(uuid, "new name");
        when(mockSkillRepository.findById(uuid)).thenReturn(Optional.of(skill));
        when(mockSkillRepository.save(skill)).thenReturn(newSkill);

        Skill savedSkill = skillService.update(uuid, newSkill);

        assertEquals("new name", savedSkill.getName());
        verify(mockSkillRepository, times(1)).findById(uuid);
        verify(mockSkillRepository, times(1)).save(skill);
    }

    @Test
    void testUpdateSkillNotFound() {
        UUID uuid = UUID.randomUUID();
        Skill skill = new Skill(uuid, "new name");
        when(mockSkillRepository.findById(uuid)).thenThrow(new SkillNotFoundException(uuid));

        assertThrows(SkillNotFoundException.class, () -> skillService.update(uuid, skill));
        verify(mockSkillRepository, times(1)).findById(uuid);
        verify(mockSkillRepository, times(0)).save(any(Skill.class));
    }

    @Test
    void testSave() {
        UUID uuid = UUID.randomUUID();
        Skill newSkill = new Skill(uuid, "new name");
        when(mockSkillRepository.save(newSkill)).thenReturn(newSkill);

        Skill savedSkill = skillService.save(newSkill);

        assertEquals(newSkill, savedSkill);
        verify(mockSkillRepository, times(1)).save(newSkill);
    }

    @Test
    void testDelete() {
        Skill skill = skillList.get(3);
        doNothing().when(mockSkillRepository).deleteById(skill.getId());

        skillService.delete(skill.getId());
        verify(mockSkillRepository, times(1)).deleteById(skill.getId());
    }

    private List<Skill> createMockSkillList() {
        return IntStream.range(0, 10)
                .mapToObj(i -> new Skill(UUID.randomUUID(), Integer.toString(i)))
                .collect(Collectors.toList());
    }
}
