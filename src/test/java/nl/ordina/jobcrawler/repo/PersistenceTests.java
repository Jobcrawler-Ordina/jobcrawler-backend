package nl.ordina.jobcrawler.repo;

import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.payload.SearchRequest;
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

import static nl.ordina.jobcrawler.repo.VacancySpecifications.vacancySearch;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * DataJpaTest can be used if you want to test JPA applications.
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

    @Test
    void testRepoFindById() {
        String sUuid = "e547d370-e5c2-4ed4-8f08-43d5a8f8d355";
        assertEquals(sUuid, vacancyRepository.findById(UUID.fromString(sUuid)).orElse(new Vacancy()).getId().toString());
    }

    @Test
    void findBySkills() {
        Pageable paging = PageRequest.of(1, 10);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setSkills(Sets.newSet("JAVA"));
        assertEquals(9, vacancyRepository.findAll(vacancySearch(searchRequest), paging).getTotalElements());
        searchRequest.setSkills(Sets.newSet("Maven"));
        assertEquals(19, vacancyRepository.findAll(vacancySearch(searchRequest), paging).getTotalElements());
        searchRequest.setSkills(Sets.newSet("Angular"));
        assertEquals(32, vacancyRepository.findAll(vacancySearch(searchRequest), paging).getTotalElements());
        searchRequest.setSkills(Sets.newSet("Maven", "Angular"));
        assertEquals(4, vacancyRepository.findAll(vacancySearch(searchRequest), paging)
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
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setKeywords("test");
        assertEquals(104, vacancyRepository.findAll(vacancySearch(searchRequest), paging).getTotalElements());
    }

    @Test
    void testFindByDistance() {
        Pageable paging = PageRequest.of(1, 10);
        SearchRequest searchRequest = new SearchRequest();
        double[] coord = { 5.24900804050379, 52.08653175 };
        searchRequest.setCoord(coord);
        searchRequest.setLocation("Zeist");
        searchRequest.setDistance(10L);
        assertEquals(20, vacancyRepository.findAll(vacancySearch(searchRequest), paging).getTotalElements());
    }

}
