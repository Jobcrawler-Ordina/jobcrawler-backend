package nl.ordina.jobcrawler.scrapers;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.service.DocumentService;
import nl.ordina.jobcrawler.service.LogService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class JobBirdScraperTest extends UseLocalSavedFiles {

    @InjectMocks
    private JobBirdScraper jobBirdScraper;

    @Mock
    private DocumentService documentServiceMock;

    private Document getDocFromUrl(String aFilename) throws IOException {
        File inputFile = getFile(aFilename);
        return Jsoup.parse(inputFile, "UTF-8", "");
    }

    @BeforeEach
    public void init() {
        // in several tests, the logService and the documentService of the testable is mocked.
        // Reset to the real logService and the real documentService
        // in case it concerns a different test case
        LogService logService = new LogService();
        DocumentService documentService = new DocumentService();
        jobBirdScraper = new JobBirdScraper();
        jobBirdScraper.setLogService(logService);
        jobBirdScraper.setDocumentService(documentService);
    }

    @Test
    void testGetVacancies() throws IOException {
        jobBirdScraper.setDocumentService(documentServiceMock);
        Document doc = getDocFromUrl("JobBird/jobbirdvacatures.htm");
        when(documentServiceMock.getDocument(anyString())).thenReturn(doc);
        List<VacancyDTO> vacancyDTOList = jobBirdScraper.getVacancies();
        assert(vacancyDTOList.get(0).getVacancyURL()).contains("www.jobbird.com");
        assertNotNull(vacancyDTOList);
    }

    @Test
    void testGetUrenPerWeek() throws IOException {
        Document doc = getDocFromUrl("JobBird/jobbird03_vacancy.htm");
        Integer result = jobBirdScraper.retrieveWorkHours(doc.select("div.card-body").text());
        assertNull(result);
    }

}
