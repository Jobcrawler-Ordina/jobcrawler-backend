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
import org.hibernate.*;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.sql.Connection;
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

    private final LocationService locationService;

    private final SkillMatcherService skillMatcherService;

    private LocationRepository locationRepository;

//    private SessionFactory sessionFactory;

    @Autowired
    public ScraperService(VacancyService vacancyService, LocationService locationService, SkillMatcherService skillMatcherService, LocationRepository locationRepository) {
        this.vacancyService = vacancyService;
        this.locationService = locationService;
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
    @Transactional
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

                    vacancy.setLocationString(vacancyLocation);

                    Set<Skill> skills = skillMatcherService.findMatchingSkills(vacancy);
                    vacancy.setSkills(skills);
                    vacancyService.save(vacancy);
                    newVacancy++;

                    if (vacancyLocation!="") {
                        Optional<Location> existCheckLocation = locationService.findByLocationName(vacancyLocation);

/*                        String finalVacancyLocation = vacancyLocation;
                        existCheckLocation.ifPresentOrElse(l -> {
                            vacancy.setLocation(l);

                        },() -> {
                            try {
                                vacancy.setLocation(LocationService.getCoordinates(finalVacancyLocation));
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });*/

                        if (!existCheckLocation.isPresent()) {
                            Location location = LocationService.getCoordinates(vacancyLocation);
//                            locationRepository.save(location);
                            locationService.save(location);
                            vacancy.setLocation(location);
                        } else {
                            UUID id = existCheckLocation.get().getId();
                            Location location = locationService.findById(id).get();
                            vacancy.setLocation(location);
/*                            List<Vacancy> vacanciesAtLocation;
                            vacanciesAtLocation = vacancyService.findByLocationid(id);
                            vacanciesAtLocation.add(vacancy);
                            location.setVacancies(vacanciesAtLocation);*/
                        }
                    }
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
