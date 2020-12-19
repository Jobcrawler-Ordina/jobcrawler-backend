package nl.ordina.jobcrawler.scrapers;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeadfirstScraperTest extends UseLocalSavedFiles {

    @InjectMocks
    private HeadfirstScraper headfirstScraper;

    @Mock
    private RestTemplate restTemplateMock;

    private static ResponseEntity<String> jsonResponse1;
    private static ResponseEntity<String> jsonResponse2;
    private static ResponseEntity<String> jsonResponse3;
    private static ResponseEntity<String> jsonResponse4;
    private static ResponseEntity<HeadfirstResponse> jsonResponse5;

    @BeforeAll
    public static void init() throws Exception {
        MultiValueMap<String,String> headers1 = new HttpHeaders();
        headers1.add("Set-cookie","SELECT-AUTH-TOKEN=123456789012345678901234567890123456789012345678");
        jsonResponse1 = new ResponseEntity<String>("",headers1,HttpStatus.OK);

        MultiValueMap<String,String> headers2 = new HttpHeaders();
        headers2.add("Location","...state=123456...");
        headers2.add("Set-cookie","SELECT-JWT-TOKEN=123456789012345678901234567890123456789012345678");
        jsonResponse2 = new ResponseEntity<String>("",headers2,HttpStatus.OK);

        MultiValueMap<String,String> headers3 = new HttpHeaders();
        headers3.add("Location","...code=123456...");
        jsonResponse3 = new ResponseEntity<String>("",headers3,HttpStatus.OK);

        MultiValueMap<String,String> headers4 = new HttpHeaders();
        headers4.add("Set-cookie","SELECT-JWT-TOKEN=123456789012345678901234567890123456789012345678");
        jsonResponse4 = new ResponseEntity<String>("",headers4,HttpStatus.OK);

        File jsonFile = getFile("/Headfirst/vacanciesResponse.json");
        HeadfirstResponse headfirstResponse = new ObjectMapper().readValue(jsonFile, HeadfirstResponse.class);
        jsonResponse5 = new ResponseEntity<>(headfirstResponse, HttpStatus.OK);
    }

    @Test
    void test_getVacancies() {
        when(restTemplateMock.exchange(eq("https://portal.select.hr/login"), any(), any(), any(Class.class))).thenReturn(jsonResponse1);
        when(restTemplateMock.exchange(eq("https://headfirst.select.hr/login"), any(), any(), any(Class.class))).thenReturn(jsonResponse2);
        when(restTemplateMock.exchange(startsWith("https://portal.select.hr/oauth/authorize"), any(), any(), any(Class.class))).thenReturn(jsonResponse3);
        when(restTemplateMock.exchange(startsWith("https://headfirst.select.hr/login?"), any(), any(), any(Class.class))).thenReturn(jsonResponse4);
        when(restTemplateMock.exchange(eq("https://headfirst.select.hr/api/v2/jobrequest/search"), any(), any(), any(Class.class))).thenReturn(jsonResponse5);
        List<VacancyDTO> vacancyDTOList = headfirstScraper.getVacancies();
        assertEquals(4, vacancyDTOList.size());
        assertEquals("Functioneel beheerder", vacancyDTOList.get(0).getTitle());
        vacancyDTOList.forEach(v -> assertNotNull(v.getPostingDate()));
        vacancyDTOList.forEach(v -> assertTrue(v.getAbout().contains("Beschrijving")));
        vacancyDTOList.forEach(v -> assertTrue(v.getVacancyURL().contains("headfirst.select.hr")));
    }
}
