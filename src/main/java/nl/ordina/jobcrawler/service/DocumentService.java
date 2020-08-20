package nl.ordina.jobcrawler.service;

import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.scrapers.VacancyScraper;
import org.jsoup.nodes.Document;

public class DocumentService {

    VacancyScraper vs;

    public Document getDocument(String aSearchURL) {
        return vs.getDocument(aSearchURL);
    }

}
