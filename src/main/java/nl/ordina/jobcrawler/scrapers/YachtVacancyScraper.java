package nl.ordina.jobcrawler.scrapers;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.model.Vacancy;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


@Slf4j
@Component
public class YachtVacancyScraper extends VacancyScraper {

    private static final String VACANCY_URL_PREFIX = "https://www.yacht.nl";

    RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public YachtVacancyScraper() {
        super(
                "https://www.yacht.nl/vacatures?_hn:type=resource&_hn:ref=r2_r1_r1&&vakgebiedProf=IT", // Required search URL. Can be retrieved using getSEARCH_URL()
                "Yacht" // Required broker. Can be retrieved using getBROKER()
        );
    }

    /**
     * This method retrieves all URLs and other available data of the page that shows multiple vacancies.
     *
     * @return List of VacancyURLs with as much details of the vacancy as possible.
     */

    @Override
    public List<Vacancy> getVacancies() {
        log.info(String.format("%s -- Start scraping", getBROKER().toUpperCase()));
        List<Vacancy> vacancies = new CopyOnWriteArrayList<>();

        int totalnumberOfPages = 1;
        for (int pageNumber = 1; pageNumber <= totalnumberOfPages; pageNumber++) {
            YachtVacancyResponse yachtVacancyResponse = scrapeVacancies(pageNumber);

            if (pageNumber == 1) {
                totalnumberOfPages = yachtVacancyResponse.getPages();
                log.info(String.format("%s -- Total number of pages: %s", getBROKER(), totalnumberOfPages));
            }

            log.info(String.format("%s -- Retrieving vacancy urls from page: %d of %d", getBROKER(), yachtVacancyResponse.getCurrentPage(), yachtVacancyResponse.getPages()));

            yachtVacancyResponse.getVacancies().parallelStream().forEach( (Map<String, Object> vacancyData) -> {
                Map<String, Object> vacancyMetaData = (Map<String, Object>) vacancyData.get("meta");
                String vacancyURL = (String) vacancyData.get("detailUrl");
                vacancyURL = vacancyURL.contains("?") ? vacancyURL.split("\\?")[0] : vacancyURL;
                vacancyURL = vacancyURL.contains("http") ? vacancyURL : VACANCY_URL_PREFIX + vacancyURL;
                Document vacancyDoc = getDocument(vacancyURL);
                Vacancy vacancy = Vacancy.builder()
                        .vacancyURL(vacancyURL)
                        .title((String) vacancyData.get("title"))
                        .hours((String) vacancyMetaData.get("hours"))
                        .broker(getBROKER())
                        .vacancyNumber((String) vacancyData.get("vacancyNumber"))
                        .location((String) vacancyMetaData.get("location"))
                        .postingDate((String) vacancyData.get("date"))
                        .about(getVacancyAbout(vacancyDoc))
                        .salary((String) vacancyMetaData.get("salary"))
                        .build();

                vacancies.add(vacancy);
                log.info(String.format("%s - Vacancy found: %s", getBROKER(), vacancy.getTitle()));
            });

        }
        log.info(String.format("%s -- Returning scraped vacancies", getBROKER()));
        return vacancies;
    }

    /**
     * This method does a get request to Yacht to retrieve the vacancies from a specific page.
     *
     * @param pageNumber Pagenumber of which the vacancy data should be retrieved
     * @return json response from the get request
     */
    private YachtVacancyResponse scrapeVacancies(int pageNumber) {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

        ResponseEntity<YachtVacancyResponse> response
                = restTemplate.getForEntity(getSEARCH_URL() + "&pagina=" + pageNumber, YachtVacancyResponse.class);

        return response.getBody();
    }

    /**
     * This method selects the vacancy details from the html document
     *
     * @param doc jsoup document of a vacancy
     */
    private String getVacancyAbout(Document doc) {
        // Extracts the about part from the vacancy
        Element vacancyBody = doc.select(".rich-text--vacancy").first();
        return vacancyBody.text();
    }

}
