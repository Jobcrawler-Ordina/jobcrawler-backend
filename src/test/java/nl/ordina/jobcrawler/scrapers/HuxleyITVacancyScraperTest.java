package nl.ordina.jobcrawler.scrapers;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ordina.jobcrawler.model.Vacancy;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class HuxleyITVacancyScraperTest {

    @InjectMocks
    private HuxleyITVacancyScraper huxleyITVacancyScraper;

    @Mock
    private RestTemplate restTemplateMock;

    private static ResponseEntity<HuxleyITResponse> jsonResponse;
    private static ResponseEntity<HuxleyITResponse> noDataResponse;

    @BeforeAll
    public static void init() throws Exception {
        // Saved .json response in resources folder is being used in this test. Content of this file is needed.
        File jsonFile = getFile("/HuxleyIT/postRequestResponse.json");
        // We need to map the data from the jsonFile according to our HuxleyItResponse.class
        HuxleyITResponse huxleyITResponse = new ObjectMapper().readValue(jsonFile, HuxleyITResponse.class);
        jsonResponse = new ResponseEntity<>(huxleyITResponse, HttpStatus.OK);

        File jsonFileNoData = getFile("/HuxleyIT/postRequestNoData.json");
        HuxleyITResponse huxleyITResponseNoData = new ObjectMapper().readValue(jsonFileNoData, HuxleyITResponse.class);
        noDataResponse = new ResponseEntity<>(huxleyITResponseNoData, HttpStatus.OK);
    }

    @Test
    public void test_getVacancies() {
        when(restTemplateMock.postForEntity(anyString(), any(), any(Class.class)))
                .thenReturn(jsonResponse);
        List<Vacancy> vacancyList = huxleyITVacancyScraper.getVacancies();
        assertEquals(4, vacancyList.size());
        assertEquals("Security Architect", vacancyList.get(0).getTitle());
    }


    // This method is used to retrieve the file content for local saved html files.
    private static File getFile(String fileName) {
        try {
            URL fileContent = HuxleyITVacancyScraperTest.class.getResource(fileName);
            return fileContent != null ? new File(fileContent.toURI()) : new File("/404");
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
