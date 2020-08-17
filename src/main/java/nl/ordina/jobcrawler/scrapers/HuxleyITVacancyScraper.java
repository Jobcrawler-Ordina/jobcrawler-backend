package nl.ordina.jobcrawler.scrapers;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.model.Vacancy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Instead of scraping the page HTML for urls, this scraper gets the vacancy data directly from the HuxleyIT API.
 *
 * The reason for this is that the HuxleyIT website does not have the possibility of showing different result pages purely by changing the page number in the url.
 * Getting this data by directly calling the API proved to not only provide the pages data, but all details of all vacancies as well.
 * Because the API already provides all vacancy details, we decided to use this data instead of separately scrape the vacancy detail page.
 */

@Slf4j
@Component
public class HuxleyITVacancyScraper extends VacancyScraper {

    private static final String VACANCY_URL_PREFIX = "https://www.huxley.com/nl-nl/job/kyc/";

    RestTemplate restTemplate = new RestTemplate();

    /**
     * Default constructor that calls the constructor from the abstract class.
     */
    @Autowired
    public HuxleyITVacancyScraper() {
        super(
                "https://api.websites.sthree.com/api/services/app/Search/Search", // Required search URL. Can be retrieved using getSEARCH_URL()
                "HuxleyIT" // Required broker. Can be retrieved using getBROKER()
        );
    }

    /**
     * Default function to start scraping vacancies.
     *
     * @return List with vacancies.
     */
    @Override
    public List<Vacancy> getVacancies() {
        /*
            The HuxleyIT API wants to know a total number of vacancies to return. When called, it also returns the total number of vacancies stored.
            In order to get all vacancies that are stored, the API is called twice:
            - The first time to read how many vacancies are stored.
            - The second time to get ALL stored vacancies by supplying this number as wanted number of results.
         */
        log.info(String.format("%s -- Start scraping", getBROKER().toUpperCase()));

        int totalVacancies = scrapeVacancies(0).getHits();
        List<Map<String, Object>> vacanciesData = scrapeVacancies(totalVacancies).getVacanciesData();

        List<Vacancy> vacancies = new ArrayList<>();
        for (Map<String, Object> vacancyData : vacanciesData) {
            Vacancy vacancy = Vacancy.builder()
                    .vacancyURL(VACANCY_URL_PREFIX + vacancyData.get("jobReference"))
                    .title((String) vacancyData.get("title"))
                    .broker(getBROKER())
                    .vacancyNumber((String) vacancyData.get("jobReference"))
                    .location((String) vacancyData.get("city"))
                    .salary((String) vacancyData.get("salaryText"))
                    .postingDate((String) vacancyData.get("postDate"))
                    .about((String) vacancyData.get("description"))
                    .build();

            vacancies.add(vacancy);
            log.info(String.format("%s - Vacancy found: %s", getBROKER(), vacancy.getTitle()));
        }
        log.info(String.format("%s -- Returning scraped vacancies", getBROKER()));
        return vacancies;
    }

    /**
     * Retrieve the vacancies from the POST API endpoint.
     *
     * @param maxNrOfVacancies Used for sending a post request to retrieve all available vacancies from HuxleyIT
     * @return HuxleyITResponse with needed attributes to fill the Vacancy entity.
     */
    private HuxleyITResponse scrapeVacancies(int maxNrOfVacancies) {
        /*
            We are using the Spring RestTemplate for calling the HuxleyIT API POST endpoint, which provides the vacancy data.

            For a successful http request we need to indicate in the http headers what type of data we are sending and what type of data we expect in return.
            This is JSON in both cases.

            The needed body for the POST request is duplicated from their frontend.
            These parameters are noteworthy:
            - "resultFrom": This indicates from which result number the API should start when returning results (we could use this to skip the first 50 results for example).
            - "resultSize": This indicates the maximum number of results we want to get from the API. The API will return less results if this number is higher than the actual number of stored vacancies.
            = "country": This indicates what country we want the returned vacancies to be located in.
         */

        // Configure headers for request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Configure request body parameters
        String[] country = {"Nederland"};
        Map<String, Object> body = new HashMap<>();
        body.put("resultSize", maxNrOfVacancies);
        body.put("language", "nl-nl");
        body.put("country", country);
        body.put("brandCode", "HA");

        // Build and trigger the request
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<HuxleyITResponse> response = restTemplate.postForEntity(getSEARCH_URL(), entity, HuxleyITResponse.class);

        // Return the result
        return response.getBody();
    }

}
