package nl.ordina.jobcrawler.service;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.scrapers.HuxleyITVacancyScraper;
import nl.ordina.jobcrawler.scrapers.JobBirdScraper;
import nl.ordina.jobcrawler.scrapers.YachtVacancyScraper;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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
    private final LocationService locationService;
    private final YachtVacancyScraper yachtVacancyScraper;
    private final HuxleyITVacancyScraper huxleyITVacancyScraper;
    private final JobBirdScraper jobBirdScraper;

    public ScraperService(VacancyService vacancyService, LocationService locationService,
                          YachtVacancyScraper yachtVacancyScraper, HuxleyITVacancyScraper huxleyITVacancyScraper,
                          JobBirdScraper jobBirdScraper) {
        this.vacancyService = vacancyService;
        this.locationService = locationService;
        this.yachtVacancyScraper = yachtVacancyScraper;
        this.huxleyITVacancyScraper = huxleyITVacancyScraper;
        this.jobBirdScraper = jobBirdScraper;
    }

    @Scheduled(cron = "0 0 12,18 * * *")
    // Runs two times a day. At 12pm and 6pm
    @Transactional
    public void scrape() {
        log.info("CRON Scheduled -- Scrape vacancies");

        List<VacancyDTO> allVacancyDTOs = startScraping();
        Vacancy vacancy;
        int existVacancy = 0;
        int newVacancy = 0;

        for (VacancyDTO vacancyDTO : allVacancyDTOs) {
            try {
                Optional<Vacancy> existCheck = vacancyService.findByURL(vacancyDTO.getVacancyURL());
                if (existCheck.isPresent()) {
                    existVacancy++;
                } else {
                    vacancy = Vacancy.builder()
                            .vacancyURL(vacancyDTO.getVacancyURL())
                            .title(vacancyDTO.getTitle())
                            .broker(vacancyDTO.getBroker())
                            .vacancyNumber(vacancyDTO.getVacancyNumber())
                            .hours(vacancyDTO.getHours())
                            .salary(vacancyDTO.getSalary())
                            .postingDate(vacancyDTO.getPostingDate())
                            .about(vacancyDTO.getAbout())
                            .company(vacancyDTO.getCompany())
                            .build();
                    String vacancyLocation = vacancyDTO.getLocationString();
                    if(vacancyLocation.endsWith(", Nederland")) {vacancyLocation = vacancyLocation.substring(0,vacancyLocation.length()-11);}
                    if(vacancyLocation.endsWith(", Netherlands")) {vacancyLocation = vacancyLocation.substring(0,vacancyLocation.length()-13);}
                    if(vacancyLocation.endsWith(", the Netherlands")) {vacancyLocation = vacancyLocation.substring(0,vacancyLocation.length()-16);}
                    if(vacancyLocation.equals("'s-Hertogenbosch")) {vacancyLocation = "Den Bosch";}
                    if (!vacancyLocation.equals("")) {
                        Optional<Location> existCheckLocation = locationService.findByLocationName(vacancyLocation);
                        if (existCheckLocation.isEmpty()) {
                            Location location = new Location(vacancyLocation, locationService.getCoordinates(vacancyLocation));
                            locationService.save(location);
                            vacancy.setLocation(location);
                            vacancyService.save(vacancy);
                        } else {
                            Location location = existCheckLocation.get();
                            vacancy.setLocation(location);
                            vacancyService.save(vacancy);
                        }
                    } else {
                        vacancyService.save(vacancy);
                    }
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
        allVacancyDTOs.clear();
    }

    @Scheduled(cron = "0 30 11,17 * * *") // Runs two times a day. At 11.30am and 5.30pm.
    public void deleteNoMoreExistingVacancies() {
        log.info("CRON Scheduled -- Started deleting non-existing jobs");
        // Change this to find all with invalid url eg non-existing job
        List<Vacancy> vacanciesToDelete = vacancyService.findAll();
        vacanciesToDelete.removeIf(vacancyService::hasValidURL);

        log.info(vacanciesToDelete.size() + " vacancies to delete.");

        for (Vacancy vacancyToDelete : vacanciesToDelete) {
            vacancyService.delete(vacancyToDelete.getId());
        }
        log.info("Finished deleting non-existing jobs");
    }

    private List<VacancyDTO> startScraping() {
        List<VacancyDTO> vacancyDTOsList = new CopyOnWriteArrayList<>();
        Arrays.asList(yachtVacancyScraper, huxleyITVacancyScraper, jobBirdScraper)
                .parallelStream().forEach(vacancyScraper -> vacancyDTOsList
                .addAll(vacancyScraper.getVacancies()));
        return vacancyDTOsList;
    }

}
