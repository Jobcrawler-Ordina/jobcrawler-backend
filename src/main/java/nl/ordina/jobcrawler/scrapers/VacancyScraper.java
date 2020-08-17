package nl.ordina.jobcrawler.scrapers;

import nl.ordina.jobcrawler.model.Vacancy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

abstract public class VacancyScraper {

    private final String SEARCH_URL;
    private final String BROKER;

    /**
     * Constructor for abstract class VacancyScraper
     *
     * @param url Default seach url for scraper
     * @param broker Used broker for scraper
     */
    public VacancyScraper(String url, String broker) {
        this.SEARCH_URL = url;
        this.BROKER = broker;
    }

    /**
     * The getDocument function retrieves the html page from the given url
     *
     * @param url Url from page that needs to be retrieved
     * @return Jsoup Document
     */
    public static Document getDocument(final String url) {
        try {
            String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36 ArabotScraper";
            return Jsoup.connect(url).userAgent(userAgent).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return Returns SEARCH_URL
     */
    public String getSEARCH_URL() {
        return SEARCH_URL;
    }

    /**
     * @return Returns BROKER
     */
    public String getBROKER() {
        return BROKER;
    }

    /**
     * Method that needs to be overwritten by scrapers extending this class
     *
     * @return List of vacancies
     */
    abstract public List<Vacancy> getVacancies();

}
