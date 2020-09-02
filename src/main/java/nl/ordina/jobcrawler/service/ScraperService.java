package nl.ordina.jobcrawler.service;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.repo.LocationRepository;
import nl.ordina.jobcrawler.scrapers.HuxleyITVacancyScraper;
import nl.ordina.jobcrawler.scrapers.JobBirdScraper;
import nl.ordina.jobcrawler.scrapers.VacancyScraper;
import nl.ordina.jobcrawler.scrapers.YachtVacancyScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
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

    private LocationRepository locationRepository;

    @Autowired
    public ScraperService(VacancyService vacancyService, SkillMatcherService skillMatcherService, LocationRepository locationRepository) {
        this.vacancyService = vacancyService;
        this.skillMatcherService = skillMatcherService;
        this.locationRepository = locationRepository;
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

                    String vacancyLocation = vacancy.getLocationString();
                    if(vacancyLocation.matches("Den Haag")) {vacancyLocation = "'s-Gravenhage";}
                    if(vacancyLocation.endsWith(", Nederland")) {vacancyLocation = vacancyLocation.substring(0,vacancyLocation.length()-11);}
                    if (vacancyLocation!="") {
                        Optional<Location> existCheckLocation = locationRepository.findByLocationName(vacancyLocation);
                        if (!existCheckLocation.isPresent()) {
                            Location location = LocationService.getCoordinates(vacancyLocation);
//                            locationRepository.save(location);
                            vacancy.setLocation(location);
                        } else {
                            Location location = existCheckLocation.get();
/*                            List<Vacancy> retrievedVacancies = location.getVacancies();
                            for (Vacancy retrievedVacancy : retrievedVacancies) {
                                Set<Skill> retrievedSkills = retrievedVacancy.getSkills();
                                retrievedVacancy.setSkills(retrievedSkills);
                            }*/
                            //System.out.println(existCheckLocation.get());
                            vacancy.setLocation(location);
//                            System.out.println(vacancy.getLocation().getVacancies().toString());
                        }
                    }

                    Set<Skill> skills = skillMatcherService.findMatchingSkills(vacancy);
                    vacancy.setSkills(skills);
                    System.out.println(vacancy.toString());
                    System.out.println(vacancy.getLocation().toString());
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
