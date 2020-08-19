package nl.ordina.jobcrawler.scrapers;

import nl.ordina.jobcrawler.model.Vacancy;
import org.jsoup.nodes.Document;

public class DocumentService {

    VacancyScraper vs;

    public Document getDocument(String aSearchURL) {
        return vs.getDocument(aSearchURL);
    }

}
