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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class YachtVacancyScraperTest {

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
        YachtVacancyResponse yachtVacancyResponseNoData = new ObjectMapper().readValue(jsonFileNoData, YachtVacancyResponse.class);
        noDataResponse = new ResponseEntity<>(yachtVacancyResponseNoData, HttpStatus.OK);
    }

    @Test
    public void test_getVacancies() {
        when(restTemplateMock.getForEntity(anyString(), any(Class.class)))
               .thenReturn(jsonResponse);
        List<Vacancy> vacancyList = yachtVacancyScraper.getVacancies();
        assertEquals(2,vacancyList.size());
        assertTrue("Moerdijk".equals(vacancyList.get(0).getLocation())
                || "Moerdijk".equals(vacancyList.get(1).getLocation()));
        assertTrue(vacancyList.get(0).getVacancyURL().contains("github"));

        verify(restTemplateMock, times(1)).getForEntity(anyString(), any(Class.class));
    }

    @Test
    public void test_getVacancies_throws_exception() {
        when(restTemplateMock.getForEntity(anyString(), any(Class.class)))
                .thenReturn(noDataResponse);

        // Calling the getVacancies() method causes a NullPointerException as the returned data gives an empty json response.
        Assertions.assertThrows(NullPointerException.class, () -> {
            List<Vacancy> vacancyList = yachtVacancyScraper.getVacancies();
        });

        verify(restTemplateMock, times(1)).getForEntity(anyString(), any(Class.class));
    }

    // This method is used to retrieve the file content for local saved html files.
    private static File getFile(String fileName) {
        try {
            URL fileContent = YachtVacancyScraperTest.class.getResource(fileName);
            return fileContent != null ? new File(fileContent.toURI()) : new File("/404");
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
