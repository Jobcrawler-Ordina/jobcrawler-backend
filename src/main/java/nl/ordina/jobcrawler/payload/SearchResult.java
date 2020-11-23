package nl.ordina.jobcrawler.payload;

import lombok.Data;

import java.util.List;

@Data
public class SearchResult {

    private List<VacancyDTO> vacancies;
    private int currentPage;
    private long totalItems;
    private int totalPages;

}
