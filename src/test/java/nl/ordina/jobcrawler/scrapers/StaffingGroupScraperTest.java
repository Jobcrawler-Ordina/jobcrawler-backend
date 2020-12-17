package nl.ordina.jobcrawler.scrapers;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.service.DocumentService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class StaffingGroupScraperTest extends UseLocalSavedFiles {

    @InjectMocks
    private StaffingGroupScraper staffingGroupScraper;

    @Mock
    private DocumentService documentServiceMock;

    private Document getDocFromUrl(String aFilename) throws IOException {
        File inputFile = getFile(aFilename);
        return Jsoup.parse(inputFile, "UTF-8", "https://www.destaffinggroep.nl/opdrachten/");
    }

    @BeforeEach
    public void init() {
        // in several tests, the logService and the documentService of the testable is mocked.
        // Reset to the real logService and the real documentService
        // in case it concerns a different test case
        DocumentService documentService = new DocumentService();
        staffingGroupScraper = new StaffingGroupScraper();
        staffingGroupScraper.setDocumentService(documentService);
    }

    @Test
    void testGetVacancies() throws IOException {
        staffingGroupScraper.setDocumentService(documentServiceMock);
        Document docVacs = getDocFromUrl("StaffingGroup/vacanciesPage.htm");
        Document doc1Vac = getDocFromUrl("StaffingGroup/vacancyPage.htm");
        when(documentServiceMock.getDocument(startsWith("https://www.destaffinggroep.nl/opdrachten"))).thenReturn(docVacs);
        when(documentServiceMock.getDocument(startsWith("https://www.destaffinggroep.nl/opdracht/"))).thenReturn(doc1Vac);
        List<VacancyDTO> vacancyDTOList = staffingGroupScraper.getVacancies();
        assertEquals(5, vacancyDTOList.size());
        assertEquals("Mechanical Engineer", vacancyDTOList.get(0).getTitle());
        vacancyDTOList.forEach(v -> assertNotNull(v.getPostingDate()));
        vacancyDTOList.forEach(v -> assertTrue(v.getAbout().contains("We are looking")));
        vacancyDTOList.forEach(v -> assertTrue(v.getVacancyURL().contains("www.destaffinggroep.nl")));
    }

}
