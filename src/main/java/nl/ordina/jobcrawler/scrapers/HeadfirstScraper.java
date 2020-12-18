package nl.ordina.jobcrawler.scrapers;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

@Slf4j
@Component
public class HeadfirstScraper extends VacancyScraper {

    RestTemplate restTemplate = new RestTemplate();

    public HeadfirstScraper() {
        super(
                "https://headfirst.select.hr/assignment", // Required search URL. Can be retrieved using getSEARCH_URL()
                "Headfirst" // Required broker. Can be retrieved using getBROKER()
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

        /*
        Request 1 geeft username & password mee, haalt SELECT-AUTH-TOKEN op
        Request 2 haalt state en 1e SELECT-JWT-TOKEN op.
        Request 3 geeft SELECT-AUTH-TOKEN en state mee, haalt code op
        Request 4 geeft 1e SELECT-JWT-TOKEN, code en state mee, haalt 2e SELECT-JWT-TOKEN op
        Request 5 geeft 2e SELECT-JWT-TOKEN mee, haalt vacancies op
        * */

        // Configure headers for request
        HttpHeaders headers1 = new HttpHeaders();
        headers1.add("referer","https://portal.select.hr/nl/nl/login");
        MultiValueMap<String, String> body1= new LinkedMultiValueMap<String, String>();
        body1.add("username","kees.hannema@ordina.nl");
        body1.add("password","JC0112Jt*");
        HttpEntity<MultiValueMap<String, String>> request1 = new HttpEntity<>(body1, headers1);
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        ResponseEntity<String> response1 = restTemplate.exchange("https://portal.select.hr/login", HttpMethod.POST, request1, String.class);
        String cookie = response1.getHeaders().get("Set-Cookie").get(0);
        String cookieName = "SELECT-AUTH-TOKEN=";
        int bi = cookie.indexOf(cookieName) + cookieName.length();
        String authToken = cookie.substring(bi,bi+48);

        HttpEntity<MultiValueMap<String, String>> request2 = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<String> response2 = restTemplate.exchange("https://headfirst.select.hr/login", HttpMethod.POST, request2, String.class);
        cookie = response2.getHeaders().get("Location").get(0);
        cookieName = "state=";
        bi = cookie.indexOf(cookieName) + cookieName.length();
        String state = cookie.substring(bi,bi+6);
        cookie = response2.getHeaders().get("Set-Cookie").get(0);
        cookieName = "SELECT-JWT-TOKEN=";
        bi = cookie.indexOf(cookieName) + cookieName.length();
        String jwtToken1 = cookie.substring(bi,bi+48);

        HttpHeaders headers3 = new HttpHeaders();
        headers3.add("cookie","SELECT-AUTH-TOKEN=" + authToken);
        HttpEntity<MultiValueMap<String, String>> request3 = new HttpEntity<>(headers3);
        String url =  "https://portal.select.hr/oauth/authorize?client_id=SELECT_APPLICATION&redirect_uri=https://headfirst.select.hr/login&response_type=code&state=" + state;
        ResponseEntity<String> response3 = restTemplate.exchange(url, HttpMethod.POST, request3, String.class);
        cookie = response3.getHeaders().get("Location").get(0);
        cookieName = "code=";
        bi = cookie.indexOf(cookieName) + cookieName.length();
        String code = cookie.substring(bi,bi+6);

        HttpHeaders headers4 = new HttpHeaders();
        headers4.add("cookie","SELECT-JWT-TOKEN=" + jwtToken1);
        HttpEntity<MultiValueMap<String, String>> request4 = new HttpEntity<>(headers4);
        ResponseEntity<String> response4 = restTemplate.exchange("https://headfirst.select.hr/login?code=" + code + "&state=" + state, HttpMethod.POST, request4, String.class);
        cookie = response4.getHeaders().get("Set-Cookie").get(0);
        cookieName = "SELECT-JWT-TOKEN=";
        bi = cookie.indexOf(cookieName) + cookieName.length();
        String jwtToken2 = cookie.substring(bi,bi+48);

        HttpHeaders headers5 = new HttpHeaders();
        headers5.add("cookie","SELECT-JWT-TOKEN=" + jwtToken2);
        headers5.add("content-type","application/json;charset=UTF-8");
        HttpEntity<String> request5 = new HttpEntity<String>("{\"page_start\": 0}",headers5);
        ResponseEntity<HeadfirstResponse> response5 = restTemplate.exchange("https://headfirst.select.hr/api/v2/jobrequest/search", HttpMethod.POST, request5, HeadfirstResponse.class);

        List<VacancyDTO> vacancyDTOs = new CopyOnWriteArrayList<>();
        int nrVacancies = response5.getBody().getTotal_results();
        int nrPages = (int) Math.ceil(nrVacancies/24.0);
        vacancyDTOs = getVacanciesFromPage(vacancyDTOs,response5);
        System.out.println("Finished page 1");

        for(int i = 1;i<nrPages;i++) {
            HttpEntity<String> request = new HttpEntity<String>("{\"page_start\": " + (24*i) +"}",headers5);
            ResponseEntity<HeadfirstResponse> response = restTemplate.exchange("https://headfirst.select.hr/api/v2/jobrequest/search", HttpMethod.POST, request, HeadfirstResponse.class);
            vacancyDTOs = getVacanciesFromPage(vacancyDTOs,response);
            System.out.println("Finished page " + (i+1));
        }
        log.info("{} -- Returning scraped vacancies", getBroker());
        return vacancyDTOs;
    }

    private List<VacancyDTO> getVacanciesFromPage(List<VacancyDTO> vacancyDTOs, ResponseEntity<HeadfirstResponse> response) {
        ArrayList<Map<String, Object>> vacanciesList = response.getBody().getResults();
        vacanciesList.forEach((Map<String, Object> vacancyData) -> {
            String id = vacancyData.get("id").toString();
            String vacancyURL = "https://headfirst.select.hr/assignment/" + id + "/description";
            String description = (String) vacancyData.get("description");
            String vacancyLocation = (String) vacancyData.get("region_name");
            if (vacancyLocation.equals("Diverse locaties")) {
                int si = description.toLowerCase().indexOf("naam hoofdstandplaats: ") + 23;
                int ei = description.substring(si).indexOf("<br") + si;
                vacancyLocation = description.substring(si, ei);
            }
            VacancyDTO vacancyDTO = VacancyDTO.builder()
                    .vacancyURL(vacancyURL)
                    .title((String) vacancyData.get("title"))
                    .hours((Integer) vacancyData.get("hours_per_week"))
                    .broker(getBroker())
                    .vacancyNumber(id)
                    .locationString(vacancyLocation)
                    .postingDate(LocalDateTime.parse((String) vacancyData.get("published_at"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")))
                    .about(description)
                    .company((String) vacancyData.get("safe_client_name"))
                    .build();

            vacancyDTOs.add(vacancyDTO);
            log.info("{} - Vacancy found: {}", getBroker(), vacancyDTO.getTitle());
        });
        return vacancyDTOs;
    }

}
