package nl.ordina.jobcrawler.scrapers;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.exception.HTMLStructureException;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.service.DocumentService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

@Slf4j
@Component
public class JobCatcherScraper extends VacancyScraper {

    RestTemplate restTemplate = new RestTemplate();
    private DocumentService documentService = new DocumentService();

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public JobCatcherScraper() {
        super(
                "https://jobcatcher.nl/api2/v1/requestsearch/search?", // Required search URL. Can be retrieved using getSEARCH_URL()
                "JobCatcher" // Required broker. Can be retrieved using getBROKER()
        );
    }

    /**
     * This method retrieves all URLs and other available data of the page that shows multiple vacancies.
     *
     * @return List of VacancyURLs with as much details of the vacancy as possible.
     */

    @Override
    public List<VacancyDTO> getVacancies() {
        log.info("{} -- Start scraping", getBroker().toUpperCase());
        List<VacancyDTO> vacancyDTOs = new CopyOnWriteArrayList<>();

        int nrVacancies = scrapeVacancies(0).getData().get(0).getAmount();
        nrVacancies = 10;
        List<Map<String, Object>> vacanciesList = scrapeVacancies(nrVacancies).getData().get(0).getList();

            vacanciesList.forEach((Map<String, Object> vacancyData) -> {
                String vacancyTitle = (String) vacancyData.get("jobrolename");
                String vacancyCompany = (String) vacancyData.get("requesterpartyname");
                String vacancyURL = "https://www.jobcatcher.nl/opdrachten/" + vacancyTitle.replace("/","-") + "/" + vacancyCompany.replace("/","-") + "/" + vacancyData.get("requestid");
                vacancyURL = vacancyURL.toLowerCase();
                vacancyURL = vacancyURL.replace(" ","-");
                vacancyURL = vacancyURL.replace("(","%28");
                vacancyURL = vacancyURL.replace(")","%29");
                vacancyURL = vacancyURL.replace("\"","%22");
                Document vacancyDoc = documentService.getDocument(vacancyURL);
                String vacancySalary = !(vacancyData.get("maximumpurchaseprice")==null)?(vacancyData.get("maximumpurchaseprice")) + ",- p/u":"";

                VacancyDTO vacancyDTO = VacancyDTO.builder()
                        .vacancyURL(vacancyURL)
                        .title(vacancyTitle)
                        .hours((int) Double.parseDouble(((String) vacancyData.get("availability")).replace(',','.')))
                        .broker(getBroker())
                        .vacancyNumber(vacancyData.get("requestid").toString())
                        .locationString((String) vacancyData.get("locationname"))
                        .postingDate(LocalDateTime.parse((String) vacancyData.get("publishdate"), DateTimeFormatter.ofPattern("yyyy-MM-dd[ ]['T']HH:mm:ss['Z']")))
                        .about(getVacancyAbout(vacancyDoc))
                        .salary(vacancySalary)
                        .company(vacancyCompany)
                        .build();

                vacancyDTOs.add(vacancyDTO);
                log.info("{} - Vacancy found: {}", getBroker(), vacancyDTO.getTitle());
            });
        log.info("{} -- Returning scraped vacancies", getBroker());
        return vacancyDTOs;
    }

    /**
     * This method does a get request to Yacht to retrieve the vacancies from a specific page.
     *
     * @param pageNumber Pagenumber of which the vacancy data should be retrieved
     * @return json response from the get request
     */
    private JobCatcherResponse scrapeVacancies(int nrOfVacancies) {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

        String url = getSearchUrl() + "itemsperpage=" + nrOfVacancies;

        ResponseEntity<JobCatcherResponse> response
                = restTemplate.getForEntity(url, JobCatcherResponse.class);

        return response.getBody();
    }

    /**
     * This method selects the vacancy details from the html document
     *
     * @param doc jsoup document of a vacancy
     * @return cleaned html string of vacancy body
     */
    private String getVacancyAbout(Document doc) {
        // Extracts the about part from the vacancy
        String about = "";
        Elements els1 = doc.select("h3");
        for (int i = 0; i < els1.size(); i++) {
            about = about + "<u>" + els1.get(i).text() + ":</u><br>";
            Elements els2 = els1.get(i).parent().nextElementSiblings();
            for (int j = 0; j < els2.size(); j++) {
                about = about + els2.get(j).text() + "<br>";
            }
            about = about + "<br>";
        }
        return about;
    }
}
