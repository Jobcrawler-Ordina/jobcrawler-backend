package nl.ordina.jobcrawler.scrapers;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.exception.HTMLStructureException;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.repo.LocationRepository;
import nl.ordina.jobcrawler.service.DocumentService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/*  Search is limited to URLs for ICT jobs with search term "java"
 *       Search URL will be completed later:a page number is added to the url
 *       example: page = 5
 *       https://www.jobbird.com/nl/vacature?s=java&rad=30&page=5&ot=date&c[]=ict
 *
 *       on each page including the first, links to vacancies can be found
 *       stop criterium: either the maximum number of pages (MAXNRPAGES) has been processed or
 *       the criterium "no more pages"
 *
 *       no more pages: when a page is retrieved for a page number higher than the
 *       highest page number available for the search, the last page and thus the last
 *       set of urls is returned - we can check whether one of those links is already in the set.
 *
 *       for demo purposes, the max nr of pages can be about 20, this suffices
 *       to max it out, it could be approximately 60, after a certain number of pages
 *       the vacancy date will be missing
 *
 *       In order to be able to check whether the program is still running, the vacancies are logged
 *       (log.info()). You may want to change this to log.debug().
 *
 */

@Slf4j
@Component
public class JobBirdScraper extends VacancyScraper {

    private final Pattern ymdPattern = Pattern.compile("^[0-9]{4}-(1[0-2]|0[1-9])-(3[01]|[12][0-9]|0[1-9])$");
    private final DateTimeFormatter ymdFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    LocationRepository locationRepository;

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    private DocumentService documentService = new DocumentService();

    private static final int MAX_NR_OF_PAGES = 25;  // 25 seems enough for demo purposes, can be up to approx 60
    // at a certain point the vacancy date will be missing


    /**
     * Default constructor that calls the constructor from the abstract class.
     */

    public JobBirdScraper() {
        super(
                "https://www.jobbird.com/nl/vacature?s=java&rad=30&page=", // Required search URL. Can be retrieved using getSEARCH_URL()
                "Jobbird" // Required broker. Can be retrieved using getBROKER()
        );
    }

    /**
     * Default function to start scraping vacancies.
     *
     * @return List with vacancies.
     */
    @Override
    public List<VacancyDTO> getVacancies() {
        List<String> vacancyURLs = retrieveURLs();
        return retrieveVacancies(vacancyURLs);
    }

    private List<String> retrieveURLs() {
        log.info("{} -- Start scraping", getBroker().toUpperCase());
        return getVacancyURLs();
    }

    private List<VacancyDTO> retrieveVacancies(List<String> vacancyURLs) {
        List<VacancyDTO> vacancies = new CopyOnWriteArrayList<>();

        vacancyURLs.parallelStream().forEach(vacancyURL -> {
            Document doc = documentService.getDocument(vacancyURL);
            if (doc != null) {
                VacancyDTO vacancyDTO = VacancyDTO.builder()
                        .vacancyURL(vacancyURL)
                        .title(getVacancyTitle(doc))
                        .hours(retrieveWorkHours(doc.select("div.card-body").text()))
                        .broker(getBroker())
                        .locationString(getLocation(doc))
                        .postingDate(getPublishDate(doc))
                        .about(getVacancyAbout(doc))
                        .company(getCompanyName(doc))
                        .build();

                vacancies.add(vacancyDTO);

                log.info("{} - Vacancy found: {}", getBroker(), vacancyDTO.getTitle());
            }
        });

        log.info("{} -- Returning scraped vacancies", getBroker());

        return vacancies;
    }

    /**
     * Create seach url based on pageNumber.
     *
     * @param pageNumber number that's needed to create search url.
     * @return String, full search url for specific page.
     */
    private String createSearchURL(int pageNumber) {
        return String.format("%s%d&ot=date&c[]=ict", getSearchUrl(), pageNumber);
    }


    /**
     * Retrieve all vacancyURLs from JobBird.
     *
     * @return A list of Strings containing the urls to the vacancies.
     */
    private List<String> getVacancyURLs() {
        //  Returns a List with VacancyURLs
        ArrayList<String> vacancyURLs = new ArrayList<>();

        try {
            Document doc = documentService.getDocument(createSearchURL(1));

            boolean continueSearching = true;
            for (int i = 1; continueSearching && i <= getLastPageToScrape(doc); i++) {
                String searchURL = createSearchURL(i);
                doc = documentService.getDocument(searchURL);

                ArrayList<String> vacancyUrlsOnPage = retrieveVacancyURLsFromDoc(doc);

                continueSearching = continueSearching(vacancyURLs, vacancyUrlsOnPage);

                if (continueSearching) {
                    vacancyURLs.addAll(vacancyUrlsOnPage);
                }
            }
        } catch (HTMLStructureException e) {
            log.error(e.getMessage());
        }

        return vacancyURLs;
    }

    /**
     * Continue searching if this page only contains new vacancies. If any of the vacancies is already know, stop searching.
     *
     * @param vacancyURLs       known vacancyURLs for this scraping session
     * @param vacancyUrlsOnPage VanacyURLS on the current page
     * @return true if none of the vacancies on this page has been encountered before in this scraping session
     */
    private boolean continueSearching(ArrayList<String> vacancyURLs, ArrayList<String> vacancyUrlsOnPage) {
        for (String vacancyUrlOnPage : vacancyUrlsOnPage) {
            if (vacancyURLs.contains(vacancyUrlOnPage)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the index of the last page to scrape
     *
     * @param doc The HTML document containing the URLs to the vacancies
     * @return the index of the last page to scrape
     */
    private int getLastPageToScrape(Document doc) {
        int totalNumberOfPages = getTotalNumberOfPages(doc);
        // TODO: we could get more sophisticated logic in place to limit the number of pages.
        // For example, we could look at the posting date of each vacancy, and limit it to thirty days.
        return Math.min(totalNumberOfPages, MAX_NR_OF_PAGES);

    }

    /* Not used - the number of pages in the block under the page is steadily extended
     * each time the next URL is generated and the corresponding page is fetched.
     * Instead, a maximum is used for the number of pages accessed.
     *
     * Find the total number of pages, this can be found via the maneuver block at the bottom of the page
     * search for <span elements with class "page-link"
     * from this page link element to the parent of the parent and then look up the children
     * these are <li elements with as attribute value the number of the page
     * continue until the page link with the text "next"
     */
    private int getTotalNumberOfPages(Document doc) {
        try {
            Elements elements = doc.select("span.page-link");
            Element parent = elements.first().parent().parent();
            Elements children = parent.children();
            int count = 0;
            for (Element child : children) {
                String text = child.text();
                if (!text.equalsIgnoreCase("volgende"))
                    count++;
            }

            log.info("{} -- Total number of pages: {}", getBroker(), count);
            return count;
        } catch (Exception e) {
            throw new HTMLStructureException(e.getLocalizedMessage());
        }
    }

    /*
     *    Retrieve the links to the individual pages for each assignment
     */
    private ArrayList<String> retrieveVacancyURLsFromDoc(Document doc) {
        ArrayList<String> result = new ArrayList<>();
        Elements elements = doc.select("div.jobResults");
        Element element = elements.first();
        Element parent = element.parent();
        Elements lijst = parent.children();

        Elements httplinks = lijst.select("a[href]");
        for (Element e : httplinks) {
            String sLink = e.attr("abs:href");
            String vacancyLink = sLink.contains("?") ? sLink.split("\\?")[0] : sLink;
            result.add(vacancyLink);
        }

        return result;
    }

    /**
     * Retrieve the vacancy title
     *
     * @param doc Document which is needed to retrieve vacancy title
     * @return String vacancy title
     */
    private String getVacancyTitle(Document doc) {
        Element vacancyHeader = doc.select("h1.no-margin").first();

        if (vacancyHeader != null) {
            return vacancyHeader.text();
        }

        return "";
    }

    /**
     * Retrieve location from vacancy
     *
     * @param doc Document which is needed to retrieve vacancy location
     * @return String vacancy location
     */
    private String getLocation(Document doc) {
        Elements elements = doc.select("span.job-result__place");
        if (!elements.isEmpty()) {
            Element jobPlace = elements.get(0);
            if (jobPlace != null) {
                return jobPlace.text();
            }
        }

        return "";
    }

    /**
     * Retrieve publishing date from vacancy
     *
     * @param doc Document which is needed to retrieve publishing date
     * @return String publish date
     */
    private LocalDateTime getPublishDate(Document doc) {
        LocalDateTime result = null;
        Elements elements = doc.select("span.job-result__place");
        if (!elements.isEmpty()) {
            Element parent = elements.get(0).parent().parent();

            Elements timeElements = parent.select("time");
            log.debug(timeElements.toString());

            if (!timeElements.isEmpty()) {
                Element timeElement = timeElements.get(0);
                String date = timeElement.attr("datetime");
                result = checkDatePattern(date) ? LocalDate.parse(date, ymdFormatter).atStartOfDay() : null;
            }
        }

        if (result == null) {
            return LocalDate.now().atStartOfDay();
        }

        return result;
    }

    private boolean checkDatePattern(String s) {
        return s != null && ymdPattern.matcher(s).matches();
    }

    /**
     * Retrieve the vacancy body to store in postgres database
     *
     * @param doc Document which is needed to retrieve the body
     * @return String vacancy body
     */
    private String getVacancyAbout(Document doc) {
        Elements aboutElements = doc.select("div#jobContent");
        return Jsoup.clean(aboutElements.html(), Whitelist.basic());
    }

    /**
     * Retrieves company name
     *
     * @param doc Document which is needed to retrieve the company name
     * @return String company name
     */
    private String getCompanyName(Document doc) {
        Elements itemListElements = doc.select("span.dashed-list__item");
        return itemListElements.last().text();
    }
}
