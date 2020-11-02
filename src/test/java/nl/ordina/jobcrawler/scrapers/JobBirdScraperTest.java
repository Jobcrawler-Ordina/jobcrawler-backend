package nl.ordina.jobcrawler.scrapers;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.exception.HTMLStructureException;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.service.DocumentService;
import nl.ordina.jobcrawler.service.LogService;
import nl.ordina.jobcrawler.service.VacancyService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class JobBirdScraperTest extends UseLocalSavedFiles {


    @InjectMocks
    private JobBirdScraper jobBirdScraperTestable;

    @Mock
    private LogService logServiceMock;

    @Mock
    private Document documentMock;

    @Mock
    private Elements elementsMock;

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
        jobBirdScraperTestable.setLogService(logService);
        jobBirdScraperTestable.setDocumentService(documentService);
    }

    @Test
    void test_getLastPageToScrape() throws HTMLStructureException, IOException {
        Document doc = getDocFromUrl("JobBird/jobbird01_should_count_5_pages.htm");
        int iLastPageToScrape = jobBirdScraperTestable.getLastPageToScrape(doc);
        assertEquals(5, iLastPageToScrape);
    }

    //happy flow results in 5 pages
    @Test
    void getTotalnrOfPagesTestFile_HappyFlow() throws IOException, HTMLStructureException {
        Document doc = getDocFromUrl("JobBird/jobbird01_should_count_5_pages.htm");
        int count = jobBirdScraperTestable.getTotalNumberOfPages(doc);
        assertEquals(5, count);
    }

    /* page structure altered, page number section not found
     * should return zero */
    @Test
    void getTotalnrOfPagesTestFile_invalidPageStructure() throws HTMLStructureException, IOException {
        Document doc = Jsoup.parse(getFile("JobBird/jobbird02_invpage.htm"), "UTF-8", "");
        Assertions.assertThrows(HTMLStructureException.class, () -> jobBirdScraperTestable
                .getTotalNumberOfPages(doc));
    }

    /* build mock document by using elements with html doc structure for 2 pages, check the happy flow
     * it turns out it is hard to mock a document by creating it this way,
     * as a consequence, html files are used instead in the rest of this module
     */
    @Test
    void getTotalnrOfPagesTest_HappyFlow() {


        Element el1 = new Element("el1");
        Element el2 = new Element("el2");
        Element el3 = new Element("el3");

        Elements children = new Elements();
        children.add(el1);
        children.add(el2);
        Element parent1 = new Element("parent1");
        parent1.appendChild(el1);
        parent1.appendChild(el2);
        Element parent2 = new Element("parent2");
        parent2.appendChild(parent1);
        parent2.appendChild(el3);

        when(documentMock.select("span.page-link")).thenReturn(children);
        int count = jobBirdScraperTestable.getTotalNumberOfPages(documentMock);
        assertEquals(2, count);
    }

    /* set vacancy title using vacancy htm file */
    @Test
    void setVacancyTitle_HappyFlow() throws IOException {
        Document doc = getDocFromUrl("JobBird/jobbird03_vacancy.htm");
        String result = jobBirdScraperTestable.getVacancyTitle(doc);
        assertEquals("Applications Engineering - Software Engineering Internship (Fall 2020)", result);
    }


    /*
     *  If title cannot be found in the html a HTMLStructureException should be thrown
     */
    @Test
    void setVacancyTitle_invalidPageStructure() throws IOException {
        Document doc = getDocFromUrl("JobBird/jobbird03_vacancy_notitle.htm");
        assertEquals("", jobBirdScraperTestable.getVacancyTitle(doc));
    }

    /*
     *  Happy flow, location, hours and date exist in page
     */
    @Test
    void setVacancySpecifics_happyFlow() throws IOException {
        Document doc = getDocFromUrl("JobBird/jobbird04_vacancyspecifics.htm");
        VacancyDTO vacancyDTO = VacancyDTO.builder().build();
        vacancyDTO.setHours(jobBirdScraperTestable.retrieveWorkHours(doc.select("div.card-body").text()));
        vacancyDTO.setLocationString(jobBirdScraperTestable.getLocation(doc));
        vacancyDTO.setPostingDate(jobBirdScraperTestable.getPublishDate(doc));
        assertEquals("Apeldoorn", vacancyDTO.getLocationString());
        assertEquals(32, vacancyDTO.getHours());
        LocalDateTime expectedDate =
                LocalDateTime.parse("2020-05-30 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        assertEquals(expectedDate, vacancyDTO.getPostingDate());
    }


    /* Unhappy flow, either location, hours or date cannot be located, an empty string should be returned
     */
    @Test
    void setVacancySpecifics_Missing() throws IOException {
        Document doc = getDocFromUrl("JobBird/jobbird04_vacancyspecifics_missing.htm");
        VacancyDTO vacancyDTO = VacancyDTO.builder().build();
        vacancyDTO.setHours(jobBirdScraperTestable.retrieveWorkHours(doc.select("div.card-body").text()));
        vacancyDTO.setLocationString(jobBirdScraperTestable.getLocation(doc));
        vacancyDTO.setPostingDate(jobBirdScraperTestable.getPublishDate(doc));
        assertEquals("", vacancyDTO.getLocationString());
        assertNull(vacancyDTO.getHours());
    }

    @Test
    void testGetUrenPerWeek() throws IOException {
        Document doc = getDocFromUrl("JobBird/jobbird03_vacancy.htm");
        Integer result = jobBirdScraperTestable.retrieveWorkHours(doc.select("div.card-body").text());
        assertNull(result);
    }

    @Test
    void testRetrieveVacancyURLS() throws IOException {
        Document doc = getDocFromUrl("JobBird/jobbirdvacatures.htm");
        ArrayList<String> vacancyURLS = jobBirdScraperTestable.retrieveVacancyURLsFromDoc(doc);
        System.out.println("nr of vacancy URLS" + vacancyURLS.size());
        assertEquals(15, vacancyURLS.size());
        assertEquals("https://www.jobbird.com/nl/vacature/10424942-integratie-tester", vacancyURLS.get(2));
    }

    @Test
    void testContinueSearching() {
        ArrayList<String> vacancyURLs = new ArrayList<>();
        ArrayList<String> vacancyURLsOnPage = new ArrayList<>();

        boolean resultWhenEmpty = jobBirdScraperTestable.continueSearching(vacancyURLs, vacancyURLsOnPage);
        assertTrue(resultWhenEmpty);

        vacancyURLs.add("the same url");
        vacancyURLsOnPage.add("a different url");
        boolean resultWhenDifferent = jobBirdScraperTestable.continueSearching(vacancyURLs, vacancyURLsOnPage);
        assertTrue(resultWhenDifferent);

        vacancyURLsOnPage.add("the same url");
        boolean resultWhenContainsTheSame = jobBirdScraperTestable.continueSearching(vacancyURLs, vacancyURLsOnPage);
        assertFalse(resultWhenContainsTheSame);
    }


    @Test
    void testGetVacancyURLs() throws IOException {

        Document overviewPage = getDocFromUrl("JobBird/jobbird01_should_count_5_pages.htm");

        when(documentServiceMock.getDocument(anyString())).thenReturn(overviewPage);
        jobBirdScraperTestable.setLogService(logServiceMock);
        jobBirdScraperTestable.setDocumentService(documentServiceMock);

        List<String> resultList = jobBirdScraperTestable.getVacancyURLs();
        assertEquals(15, resultList.size());
        assertTrue(resultList.contains("https://www.jobbird.com/nl/vacature/10216416-senior-software-engineer-backend-energy"));

        Document wrongStructurePage = getDocFromUrl("JobBird/jobbird02_invpage.htm");

        when(documentServiceMock.getDocument(anyString())).thenReturn(wrongStructurePage);
        resultList = jobBirdScraperTestable.getVacancyURLs();
        assertEquals(0, resultList.size());
        verify(logServiceMock, times(1)).logError("HTML structure altered in a critical way:null");
    }

    @Test
    void testRetrieveURLs() throws Exception {
        Document overviewPage = getDocFromUrl("JobBird/jobbird01_should_count_5_pages.htm");

        when(documentServiceMock.getDocument(anyString())).thenReturn(overviewPage);
        jobBirdScraperTestable.setLogService(logServiceMock);
        jobBirdScraperTestable.setDocumentService(documentServiceMock);

        List<String> resultList = jobBirdScraperTestable.retrieveURLs();
        verify(logServiceMock, times(1)).logInfo("JOBBIRD -- Start scraping");
        assertEquals(15, resultList.size());
        assertEquals("https://www.jobbird.com/nl/vacature/10216416-senior-software-engineer-backend-energy", resultList.get(0));
    }


    @Test
    void testGetVacancies() throws Exception {
        List<String> urlList = new ArrayList<>();
        urlList.add("dummy url");
        Document doc = getDocFromUrl("JobBird/jobbird03_vacancy.htm");

        jobBirdScraperTestable.setLogService(logServiceMock);
        jobBirdScraperTestable.setDocumentService(documentServiceMock);
        when(documentServiceMock.getDocument(anyString())).thenReturn(doc);

        List<VacancyDTO> vacancyDTOs = jobBirdScraperTestable.retrieveVacancies(urlList);
        List<Vacancy> vacancies = convertVacancyDTOs(vacancyDTOs);
        assertEquals(1, vacancies.size());
        Vacancy vacancy = vacancies.get(0);

        assertEquals("Applications Engineering - Software Engineering Internship (Fall 2020)",
                vacancy.getTitle());
    }

    @Test
    void testRetrieveVacancyPostingDate() throws IOException {
        Document doc = getDocFromUrl("JobBird/jobbirdvacatures.htm");
        jobBirdScraperTestable.setDocumentService(documentServiceMock);
        when(documentServiceMock.getDocument(anyString())).thenReturn(doc);
        List<VacancyDTO> vacancyDTOs = jobBirdScraperTestable.retrieveVacancies(Collections.singletonList("dummy url"));
        assertNotNull(vacancyDTOs.get(0).getPostingDate());
    }

    private List<Vacancy> convertVacancyDTOs(List<VacancyDTO> vacancyDTOs) {
        return vacancyDTOs.stream().map(this::convertVacancyDTO).collect(Collectors.toList());
    }

    private Vacancy convertVacancyDTO(VacancyDTO vacancyDTO) {
        return Vacancy.builder()
                .vacancyURL(vacancyDTO.getVacancyURL())
                .title(vacancyDTO.getTitle())
                .broker(vacancyDTO.getBroker())
                .vacancyNumber(vacancyDTO.getVacancyNumber())
                .hours(vacancyDTO.getHours())
                .salary(vacancyDTO.getSalary())
                .postingDate(vacancyDTO.getPostingDate())
                .about(vacancyDTO.getAbout())
                .company(vacancyDTO.getCompany())
                .build();
    }
}
