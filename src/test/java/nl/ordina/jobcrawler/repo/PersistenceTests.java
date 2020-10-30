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
        String sUuid = "30324ab8-29fd-4f23-a4da-bc445396e79a";
        assertEquals(sUuid, vacancyRepository.findById(UUID.fromString(sUuid)).orElse(new Vacancy()).getId().toString());
    }

    @Test
    void findBySkills() {
        Pageable paging = PageRequest.of(1, 10);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setSkills(Sets.newSet("JAVA"));
        assertEquals(10, vacancyRepository.findAll(vacancySearch(searchRequest), paging).getTotalElements());
        searchRequest.setSkills(Sets.newSet("Maven"));
        assertEquals(29, vacancyRepository.findAll(vacancySearch(searchRequest), paging).getTotalElements());
        searchRequest.setSkills(Sets.newSet("Angular"));
        assertEquals(31, vacancyRepository.findAll(vacancySearch(searchRequest), paging).getTotalElements());
        searchRequest.setSkills(Sets.newSet("Maven", "Angular"));
        assertEquals(6, vacancyRepository.findAll(vacancySearch(searchRequest), paging)
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
        assertEquals(112, vacancyRepository.findAll(vacancySearch(searchRequest), paging).getTotalElements());
    }

    @Test
    void testFindByDistance() {
        Pageable paging = PageRequest.of(1, 10);
        SearchRequest searchRequest = new SearchRequest();
        double[] coord = { 52.08653175, 5.24900804050379 };
        searchRequest.setCoord(coord);
        searchRequest.setLocation("Zeist");
        searchRequest.setDistance(10L);
        assertEquals(21, vacancyRepository.findAll(vacancySearch(searchRequest), paging).getTotalElements());
    }

}
