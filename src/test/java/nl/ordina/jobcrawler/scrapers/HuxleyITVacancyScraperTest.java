package nl.ordina.jobcrawler.scrapers;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.payload.VacancyDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class HuxleyITVacancyScraperTest extends UseLocalSavedFiles {

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
    void test_getVacancies() {
        when(restTemplateMock.postForEntity(anyString(), any(), any(Class.class)))
                .thenReturn(jsonResponse);
        List<VacancyDTO> vacancyDTOList = huxleyITVacancyScraper.getVacancies();
        assertEquals(4, vacancyDTOList.size());
        assertEquals("Security Architect", vacancyDTOList.get(0).getTitle());
        vacancyDTOList.forEach(v -> assertNotNull(v.getPostingDate()));
    }

}
