package nl.ordina.jobcrawler;

import lombok.Data;
import nl.ordina.jobcrawler.model.Vacancy;

import java.util.List;

@Data
public class SearchResult {

    private List<Vacancy> vacancies;
    private int currentPage;
    private long totalItems;
    private int totalPages;

}
