package nl.ordina.jobcrawler.repo;

import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static nl.ordina.jobcrawler.repo.VacancySpecifications.findBySkill;
import static nl.ordina.jobcrawler.repo.VacancySpecifications.findByValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @DataJpaTest can be used if you want to test JPA applications.
 * By default it will configure an in-memory embedded database,
 * scan for @Entity classes and configure Spring Data JPA repositories.
 *
 * Regular @Component beans will not be loaded into the ApplicationContext.
 *
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
class PersistenceTests {

    @Autowired
    private VacancyRepository vacancyRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Test
    void testRepoFindById() {
        String sUuid = "e547d370-e5c2-4ed4-8f08-43d5a8f8d355";
        assertEquals(sUuid, vacancyRepository.findById(UUID.fromString(sUuid)).orElse(new Vacancy()).getId().toString());
    }

    @Test
    void findBySkills() {
        Pageable paging = PageRequest.of(1, 10);
        assertEquals(9, vacancyRepository.findAll(findBySkill(Sets.newSet("JAVA")), paging).getTotalElements());
        assertEquals(19, vacancyRepository.findAll(findBySkill(Sets.newSet("Maven")), paging).getTotalElements());
        assertEquals(32, vacancyRepository.findAll(findBySkill(Sets.newSet("Angular")), paging).getTotalElements());
        assertEquals(4, vacancyRepository.findAll(findBySkill(Sets.newSet("Maven", "Angular")), paging)
                .getTotalElements());

    }

    @Test
    void testSkillRepo() {
        List<Skill> skills = skillRepository.findByOrderByNameAsc();
        assertEquals(5, skills.size());
    }

    @Test
    void testFindByValue() {
        Pageable paging = PageRequest.of(1, 10);
        assertEquals(104, vacancyRepository.findAll(findByValue("test"), paging).getTotalElements());
    }

}
