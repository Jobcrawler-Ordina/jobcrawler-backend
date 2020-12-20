package nl.ordina.jobcrawler.scrapers;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.service.DocumentService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class JobCatcherScraperTest extends UseLocalSavedFiles {

    @InjectMocks
    private JobCatcherScraper jobCatcherScraper;

    @Mock
    private RestTemplate restTemplateMock;
    @Mock
    private DocumentService documentServiceMock;
    private static ResponseEntity<JobCatcherResponse> jsonResponse;

    private Document getDocFromUrl(String aFilename) throws IOException {
        File inputFile = getFile(aFilename);
        return Jsoup.parse(inputFile, "UTF-8");
    }

    @BeforeAll
    public static void init() throws Exception {
        File jsonFile = getFile("/JobCatcher/vacanciesResponse.json");
        JobCatcherResponse jobCatcherResponse = new ObjectMapper().readValue(jsonFile, JobCatcherResponse.class);
        jsonResponse = new ResponseEntity<>(jobCatcherResponse, HttpStatus.OK);
    }

    @Test
    void test_getVacancies() throws IOException {
        Document doc = getDocFromUrl("JobCatcher/vacancyDoc.htm");
        jobCatcherScraper.setDocumentService(documentServiceMock);
        when(documentServiceMock.getDocument(anyString())).thenReturn(doc);
        when(restTemplateMock.getForEntity(anyString(),any(Class.class))).thenReturn(jsonResponse);
        List<VacancyDTO> vacancyDTOList = jobCatcherScraper.getVacancies();
        assertEquals(4, vacancyDTOList.size());
        assertEquals("Security Engineer", vacancyDTOList.get(0).getTitle());
        vacancyDTOList.forEach(v -> assertNotNull(v.getPostingDate()));
        vacancyDTOList.forEach(v -> assertTrue(v.getAbout().contains("Beschrijving")));
        vacancyDTOList.forEach(v -> assertTrue(v.getVacancyURL().contains("www.jobcatcher.nl")));
    }
}
