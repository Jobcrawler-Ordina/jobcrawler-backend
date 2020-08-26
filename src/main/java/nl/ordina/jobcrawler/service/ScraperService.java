package nl.ordina.jobcrawler.service;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.model.City;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.repo.CityRepository;
import nl.ordina.jobcrawler.scrapers.HuxleyITVacancyScraper;
import nl.ordina.jobcrawler.scrapers.JobBirdScraper;
import nl.ordina.jobcrawler.scrapers.VacancyScraper;
import nl.ordina.jobcrawler.scrapers.YachtVacancyScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/*
This 'starter' class uses the @Scheduled annotation. Every 15 minutes it executes the cronJobSch() function to retrieve all vacancies.
Upon fetching the vacancies it runs a check to verify if the vacancy is already present in the database.
*/

@Slf4j
@Service
public class ScraperService {


    private final VacancyService vacancyService;

    private final SkillMatcherService skillMatcherService;

    private final CityRepository cityRepository;

    @Autowired
    public ScraperService(VacancyService vacancyService, SkillMatcherService skillMatcherService, CityRepository cityRepository) {
        this.vacancyService = vacancyService;
        this.skillMatcherService = skillMatcherService;
        this.cityRepository = cityRepository;
    }

    private final List<VacancyScraper> scraperList = new ArrayList<>() {
        {
            add(new YachtVacancyScraper());
            add(new HuxleyITVacancyScraper());
            add(new JobBirdScraper());
        }
    };

    @PostConstruct
    @Scheduled(cron = "0 0 12,18 * * *") // Runs two times a day. At 12pm and 6pm
    public void scrape() {
        log.info("CRON Scheduled -- Scrape vacancies");
        List<Vacancy> allVacancies = startScraping();
        int existVacancy = 0;
        int newVacancy = 0;
        for (Vacancy vacancy : allVacancies) {
            try {
                Optional<Vacancy> existCheck = vacancyService.findByURL(vacancy.getVacancyURL());
                if (existCheck.isPresent()) {
                    existVacancy++;
                } else {
                    String vacancyLocation = vacancy.getLocation();
                    if (vacancyLocation!=null) {
                        Optional<City> existCheckCity = cityRepository.findByCityName(vacancyLocation);
                        if (!existCheckCity.isPresent()) {
                            City city = CityService.getCoordinates(vacancyLocation);
                            cityRepository.save(city);
                        }
                    }
                    Set<Skill> skills = skillMatcherService.findMatchingSkills(vacancy);
                    vacancy.setSkills(skills);
                    vacancyService.save(vacancy);
                    newVacancy++;
                }
            } catch (IncorrectResultSizeDataAccessException ie) {
                log.error("Record exists multiple times in database already!");
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        log.info(newVacancy + " new vacancies added.");
        log.info(existVacancy + " existing vacancies found.");
        log.info("Finished scraping");
        allVacancies.clear();
    }

    @Scheduled(cron = "0 30 11,17 * * *") // Runs two times a day. At 11.30am and 5.30pm.
    public void deleteNonExistingVacancies() {
        log.info("CRON Scheduled -- Started deleting non-existing jobs");
        List<Vacancy> vacanciesToDelete = vacancyService.findAll();
        vacanciesToDelete.removeIf(Vacancy::hasValidURL);

        log.info(vacanciesToDelete.size() + " vacancy to delete.");

        for (Vacancy vacancyToDelete : vacanciesToDelete) {
            vacancyService.delete(vacancyToDelete.getId());
        }
        log.info("Finished deleting non-existing jobs");
    }

    private List<Vacancy> startScraping() {
        List<Vacancy> vacanciesList = new CopyOnWriteArrayList<>();
        scraperList.parallelStream().forEach(vacancyScraper -> vacanciesList.addAll(vacancyScraper.getVacancies()));
        return vacanciesList;
    }
}
