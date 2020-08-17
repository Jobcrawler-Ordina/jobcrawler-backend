package nl.ordina.jobcrawler.scrapers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class YachtVacancyResponse {

    private Integer currentPage;
    private Integer pages;
    private ArrayList<Map<String, Object>> vacancies;

    /*
     * We don't need anything else beside some variables and lombok annotations.
     * Everything will bind because the json response from the get request is at the lowest level possible.
     * Example:
     * {
     *     "currentPage": 1,
     *     "pages": 4,
     *     "vacancies": [
     *     {
     *         "title": "randomTitle",
     *         "company": "companyName",
     *         "meta": {
     *             "hours": "40 hours",
     *             "location": "Amsterdam",
     *             "salary": "4.500"
     *         },
     *         "date": "02 juni 2020",
     *         "detailUrl": "urlToVacancy"
     *     }
     *     ],
     * }
     */

}
