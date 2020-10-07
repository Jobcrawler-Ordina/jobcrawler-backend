package nl.ordina.jobcrawler.repo;

import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.service.LocationService;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

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

    @Test
    void createVacancy() throws IOException, JSONException {

        List<Vacancy> allVacancies = new CopyOnWriteArrayList<>();

        Vacancy vacancy1 = Vacancy.builder()
                .vacancyURL("testURL")
                .title("title")
                .locationString("Werkendam")
                .company("")
                .build();

        Vacancy vacancy2 = Vacancy.builder()
                .vacancyURL("testURL2")
                .title("title")
                .locationString("Werkendam")
                .company("")
                .build();

        allVacancies.add(vacancy1);
        allVacancies.add(vacancy2);

        Location homeLocation = null;   //Temp lines for testing
        try {
            homeLocation = LocationService.getCoordinates("Diemen");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        homeLocation.setDistance(0);   //Temp lines for testing

        for (Vacancy vacancy : allVacancies) {
            try {
                Optional<Vacancy> existCheck = findByURL(vacancy.getVacancyURL());
                if (existCheck.isPresent()) {}
                else {
                    Optional<Location> existCheckLocation = findByLocationName(vacancy.getLocationString());
                    if (!existCheckLocation.isPresent()) {
                        Location location = LocationService.getCoordinates("Werkendam");
                        location.setDistance(LocationService.getDistanceFromHome(homeLocation,location));
                        vacancy.setLocation(location);
                        vacancyRepository.save(vacancy);
                    } else {
                        Location location = existCheckLocation.get();
                        vacancy.setLocation(location);
                        vacancyRepository.save(vacancy);
                    }
                }
            } catch (IncorrectResultSizeDataAccessException ie) {
                System.out.println("Record exists multiple times in database already!");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
    }
}

    public Optional<Location> findByLocationName(String locationName) {return locationRepository.findByLocationName(locationName); }

    public Optional<Vacancy> findByURL(String url) {
        return vacancyRepository.findByVacancyURLEquals(url);
    }
}
