package nl.ordina.jobcrawler.scrapers;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.exception.HTMLStructureException;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.service.DocumentService;
import nl.ordina.jobcrawler.service.LogService;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.anyString;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class JobBirdScraperTest  {


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
        ClassLoader classLoader = new JobBirdScraperTest().getClass().getClassLoader();
        File inputFile = new File(classLoader.getResource(aFilename).getFile());
        Document doc = Jsoup.parse(inputFile, "UTF-8", "");
        return doc;
    }

    @BeforeEach
    public  void init() {
        // in several tests, the logService and the documentService of the testable is mocked.
        // Reset to the real logService and the real documentService
        // in case it concerns a different test case
        LogService logService = new LogService();
        DocumentService documentService = new DocumentService();
        jobBirdScraperTestable.setLogService(logService);
        jobBirdScraperTestable.setDocumentService(documentService);
    }

    @Test
    public void test_getLastPageToScrape() throws HTMLStructureException, IOException {
        Document doc =  getDocFromUrl("JobBird/jobbird01_should_count_5_pages.htm");
        int iLastPageToScrape = jobBirdScraperTestable.getLastPageToScrape(doc);
        assertEquals(iLastPageToScrape, 5);
    }

    //happy flow results in 5 pages
    @Test
    public void getTotalnrOfPagesTestFile_HappyFlow() throws IOException, HTMLStructureException {
        Document doc = getDocFromUrl("JobBird/jobbird01_should_count_5_pages.htm");
        int count =  jobBirdScraperTestable.getTotalNumberOfPages(doc);
        assertEquals(5, count);
    }

    /* page structure altered, page number section not found
     * should return zero */
    @Test
    public void getTotalnrOfPagesTestFile_invalidPageStructure() throws IOException, HTMLStructureException {
        Assertions.assertThrows(HTMLStructureException.class,
                () -> {
                    String filename = "JobBird/jobbird02_invpage.htm";  // should count 5 pages
                    ClassLoader classLoader = new JobBirdScraperTest().getClass().getClassLoader();
                    File inputFile = new File(classLoader.getResource(filename).getFile());
                    Document doc = Jsoup.parse(inputFile, "UTF-8", "");
                    jobBirdScraperTestable.getTotalNumberOfPages(doc);
                });
    }

    /* build mock document by using elements with html doc structure for 2 pages, check the happy flow
     * it turns out it is hard to mock a document by creating it this way,
     * as a consequence, html files are used instead in the rest of this module
    */
    @Test
    public void getTotalnrOfPagesTest_HappyFlow() throws Exception {


            Element el1 = new Element("el1");
            Element el2 =  new Element("el2");
            Element el3 =  new Element("el3");

            Elements children = new Elements();
            children.add(el1);
            children.add(el2);
            Element parent1 = new Element("parent1");
            parent1.appendChild(el1);
            parent1.appendChild(el2);
            Element parent2 = new Element("parent2");
            parent2.appendChild(parent1);
            parent2.appendChild(el3);

            //when (connectionDocumentServiceMock.getConnection(anyString())).thenReturn(documentMock);
            when (documentMock.select("span.page-link")).thenReturn(children);

//            when (e2Mock.parent()).thenReturn(parent1);
            int count =  jobBirdScraperTestable.getTotalNumberOfPages(documentMock);
            assertEquals(2, count);
    }

    /* set vacancy title using vacancy htm file */
    @Test
    public void setVacancyTitle_HappyFlow() throws IOException {
        Document doc =  getDocFromUrl("JobBird/jobbird03_vacancy.htm");
        Vacancy vacancy = new Vacancy();
        String result = jobBirdScraperTestable.getVacancyTitle(doc);
        assertEquals("Applications Engineering - Software Engineering Internship (Fall 2020)", result);
    }


    /*
     *  If title cannot be found in the html a HTMLStructureException should be thrown
     */
    @Test
    public void setVacancyTitle_invalidPageStructure() throws IOException {
        Document doc = getDocFromUrl("JobBird/jobbird03_vacancy_notitle.htm");
        assert (jobBirdScraperTestable.getVacancyTitle(doc).equals(""));
    }

    /*
    *  Happy flow, location, hours and date exist in page
    */
    @Test
    public void setVacancySpecifics_happyFlow() throws IOException {
        Document doc = getDocFromUrl("JobBird/jobbird04_vacancyspecifics.htm");
        Vacancy vacancy = new Vacancy();
        vacancy.setHours(jobBirdScraperTestable.getHoursFromPage(doc));
        vacancy.setLocation(jobBirdScraperTestable.getLocation(doc));
        vacancy.setPostingDate(jobBirdScraperTestable.getPublishDate(doc));
        assertEquals("Apeldoorn", vacancy.getLocation());
        assertEquals("32", vacancy.getHours());
        assertEquals( "2020-05-30", vacancy.getPostingDate());
    }


    /* Unhappy flow, either location, hours or date cannot be located, an empty string should be returned
     */
    @Test
    public void setVacancySpecifics_Missing() throws IOException {
        Document doc = getDocFromUrl("JobBird/jobbird04_vacancyspecifics_missing.htm");
        Vacancy vacancy = new Vacancy();
        vacancy.setHours(jobBirdScraperTestable.getHoursFromPage(doc));
        vacancy.setLocation(jobBirdScraperTestable.getLocation(doc));
        vacancy.setPostingDate(jobBirdScraperTestable.getPublishDate(doc));
        assertEquals("", vacancy.getLocation());
        assertEquals("0", vacancy.getHours());
        assertEquals("", vacancy.getPostingDate());
    }

    @Test
    public void testGetUrenPerWeek() throws IOException {
        Document doc =  getDocFromUrl("JobBird/jobbird03_vacancy.htm");
        Vacancy vacancy = new Vacancy();
        String result = jobBirdScraperTestable.getHoursFromPage(doc);
        assertEquals("Full-time", result);
    }

    @Test
    public void testGetVacancyAbout() {
        when (elementsMock.text()).thenReturn("about");
        when(documentMock.select( "div.jobContainer")).thenReturn(elementsMock);
        String about = jobBirdScraperTestable.getVacancyAbout(documentMock);
        assertTrue(about.equals("about"));
    }


    @Test
    public void testRetrieveVacancyURLS() throws IOException {
        Document doc =  getDocFromUrl("JobBird/jobbirdvacatures.htm");
        ArrayList<String> vacancyURLS = jobBirdScraperTestable.retrieveVacancyURLsFromDoc(doc);
        System.out.println("nr of vacancy URLS" + vacancyURLS.size());
        assertEquals(vacancyURLS.size(), 15);
        assertEquals(vacancyURLS.get(2), "https://www.jobbird.com/nl/vacature/10424942-integratie-tester");
    }


    @Test
    public void testContinueSearching() {
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
        assertFalse( resultWhenContainsTheSame);
    }


    @Test
    public void testGetVacancyURLs() throws IOException {

        Document overviewPage =  getDocFromUrl("JobBird/jobbird01_should_count_5_pages.htm");

        when (documentServiceMock.getDocument(anyString())).thenReturn(overviewPage);
        jobBirdScraperTestable.setLogService( logServiceMock);
        jobBirdScraperTestable.setDocumentService( documentServiceMock);

        List<String> resultList = jobBirdScraperTestable.getVacancyURLs();
        assertEquals(15, resultList.size());
        assertTrue( resultList.contains("https://www.jobbird.com/nl/vacature/10216416-senior-software-engineer-backend-energy"));

        Document wrongStructurePage =  getDocFromUrl("JobBird/jobbird02_invpage.htm");

        when (documentServiceMock.getDocument(anyString())).thenReturn(wrongStructurePage);
        resultList = jobBirdScraperTestable.getVacancyURLs();
        assertEquals(0, resultList.size());
        verify(logServiceMock, times(1)).logError("HTML structure altered in a critical way:null");
    }

    @Test
    public void testRetrieveURLs() throws Exception {
        Document overviewPage =  getDocFromUrl("JobBird/jobbird01_should_count_5_pages.htm");

        when (documentServiceMock.getDocument(anyString())).thenReturn(overviewPage);
        jobBirdScraperTestable.setLogService( logServiceMock);
        jobBirdScraperTestable.setDocumentService( documentServiceMock);

        List<String> resultList = jobBirdScraperTestable.retrieveURLs();
        verify(logServiceMock, times(1)).logInfo("JOBBIRD -- Start scraping");
        assertEquals(15, resultList.size());
        assertEquals(resultList.get(0), "https://www.jobbird.com/nl/vacature/10216416-senior-software-engineer-backend-energy");
    }


    @Test
    public void testGetVacancies () throws Exception {
        List<String> urlList = new ArrayList<>();
        urlList.add("dummy url");
        Document doc =  getDocFromUrl("JobBird/jobbird03_vacancy.htm");

        jobBirdScraperTestable.setLogService( logServiceMock);
        jobBirdScraperTestable.setDocumentService( documentServiceMock);
        when (documentServiceMock.getDocument(anyString())).thenReturn(doc);

        List<Vacancy> vacancies = jobBirdScraperTestable.retrieveVacancies(urlList);
        assertEquals(1, vacancies.size());
        Vacancy vacancy = vacancies.get(0);

        assertEquals( "Applications Engineering - Software Engineering Internship (Fall 2020)",
                vacancy.getTitle());


    }
}
