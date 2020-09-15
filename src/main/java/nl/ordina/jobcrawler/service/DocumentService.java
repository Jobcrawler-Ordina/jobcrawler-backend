package nl.ordina.jobcrawler.service;

import nl.ordina.jobcrawler.scrapers.VacancyScraper;
import org.jsoup.nodes.Document;

public class DocumentService {

    public Document getDocument(String aSearchURL) {
        return VacancyScraper.getDocument(aSearchURL);
    }

}
