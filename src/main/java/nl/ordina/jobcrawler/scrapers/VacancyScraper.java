package nl.ordina.jobcrawler.scrapers;

import nl.ordina.jobcrawler.model.Vacancy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class VacancyScraper {

    private final String searchUrl;
    private final String broker;

    /**
     * Constructor for abstract class VacancyScraper
     *
     * @param url    Default seach url for scraper
     * @param broker Used broker for scraper
     */
    public VacancyScraper(String url, String broker) {
        this.searchUrl = url;
        this.broker = broker;
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
    public String getSearchUrl() {
        return searchUrl;
    }

    /**
     * @return Returns BROKER
     */
    public String getBroker() {
        return broker;
    }

    /**
     * The work hours might be hidden somewhere in the vacancy body. This method tries to split on certain words and looks for a number close to this split.
     * @param description vacancy body
     * @return Integer that can either contain the working hours per week or returns null
     */
    public Integer retrieveWorkHours(String description) {

        // Try to split on the word 'hours' and search for the first integer after a positive result of splitting
        String[] splitDescription = description.toLowerCase().split("hours| uren| uur");

        if (splitDescription.length > 1) {
            int substringEnd = Math.min(splitDescription[1].length(), 18);
            Matcher matcherBehind = matcher(splitDescription[1].substring(0, substringEnd));
            Integer hours = findHours(matcherBehind, "behind");
            if (hours != null && hours > 7) {
                return hours;
            } else {
                Matcher matcherFront = matcher(splitDescription[0].substring(splitDescription[0].length() - 10));
                Integer hoursFront = findHours(matcherFront, "front");
                return (hoursFront != null && hoursFront > 7) ? hoursFront : null;
            }
        }

        return null;
    }

    /**
     * @param input String to look for numbers
     * @return Matcher that contains matches
     */
    private Matcher matcher(String input) {
        return Pattern.compile("\\d+").matcher(input);
    }

    /**
     *
     * @param text Contains numbers that might be found
     * @param direction search direction, in front (select latest, closest to split) or behind of the split.
     * @return Integer with working hours or null
     */
    private Integer findHours(Matcher text, String direction) {
        String result = null;
        try {
            if (direction.equals("front")) {
                while (text.find()) {
                    result = text.group();
                }
            } else {
                text.find();
                result = text.group();
            }

            return Integer.valueOf(result);
        } catch (IllegalStateException | NumberFormatException e) {
            return null;
        }
    }

    /**
     * Method that needs to be overwritten by scrapers extending this class
     *
     * @return List of vacancies
     */
    abstract public List<Vacancy> getVacancies();

}
