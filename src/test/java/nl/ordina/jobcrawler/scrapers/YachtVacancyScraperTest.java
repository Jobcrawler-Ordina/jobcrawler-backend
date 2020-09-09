package nl.ordina.jobcrawler.scrapers;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ordina.jobcrawler.model.Vacancy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class YachtVacancyScraperTest extends UseLocalSavedFiles {

    @InjectMocks
    private YachtVacancyScraper yachtVacancyScraper;

    @Mock
    private RestTemplate restTemplateMock;

    private static ResponseEntity<YachtVacancyResponse> jsonResponse;
    private static ResponseEntity<YachtVacancyResponse> noDataResponse;

    @BeforeAll
    public static void init() throws Exception {
        // Saved .json response in resources folder is being used in this test. Content of this file is needed.
        File jsonFile = getFile("/yacht/getRequestResponse.json");
        // We need to map the data from the jsonFile according to our YachtVacancyResponse.class
        YachtVacancyResponse yachtVacancyResponse = new ObjectMapper().readValue(jsonFile, YachtVacancyResponse.class);
        jsonResponse = new ResponseEntity<>(yachtVacancyResponse, HttpStatus.OK);

        File jsonFileNoData = getFile("/yacht/getRequestResponseNoData.json");
        YachtVacancyResponse yachtVacancyResponseNoData = new ObjectMapper()
                .readValue(jsonFileNoData, YachtVacancyResponse.class);
        noDataResponse = new ResponseEntity<>(yachtVacancyResponseNoData, HttpStatus.OK);
    }

    @Test
    public void test_getVacancies() {
        when(restTemplateMock.getForEntity(anyString(), any(Class.class))).thenReturn(jsonResponse);
        List<Vacancy> vacancyList = yachtVacancyScraper.getVacancies();
        assertEquals(2, vacancyList.size());
        assertTrue("Moerdijk".equals(vacancyList.get(0).getLocation()) || "Moerdijk"
                .equals(vacancyList.get(1).getLocation()));
        assertTrue(vacancyList.get(0).getVacancyURL().contains("github"));
        assertNotNull(vacancyList.get(0).getPostingDate());

        verify(restTemplateMock, times(1)).getForEntity(anyString(), any(Class.class));
    }

    @Test
    public void test_getVacancies_throws_exception() {
        when(restTemplateMock.getForEntity(anyString(), any(Class.class))).thenReturn(noDataResponse);

        // Calling the getVacancies() method causes a NullPointerException as the returned data gives an empty json response.
        Assertions.assertThrows(NullPointerException.class, () -> {
            List<Vacancy> vacancyList = yachtVacancyScraper.getVacancies();
        });

        verify(restTemplateMock, times(1)).getForEntity(anyString(), any(Class.class));
    }

}
