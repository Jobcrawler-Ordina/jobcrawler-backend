package nl.ordina.jobcrawler.scrapers;

import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.service.DocumentService;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class Temp {

        public static void main(String[] args) throws IOException {

/*            final String url = "http://codeflex.co:8080/rest/Management/login";

            URLConnection connection = new URL("https://portal.select.hr/login").openConnection();
            Map<String, List<String>> hf = connection.getHeaderFields();
            List<String> cookies = hf.get("Set-Cookie");*/

            HeadfirstScraper headfirstScraper = new HeadfirstScraper();
            headfirstScraper.getVacancies();
//            System.out.println(cookies);
/*            HuxleyITVacancyScraper huxleyITVacancyScraper = new HuxleyITVacancyScraper();
            huxleyITVacancyScraper.getVacancies();*/

        }

}
