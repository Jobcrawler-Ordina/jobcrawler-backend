package nl.ordina.jobcrawler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class VacancyURLMalformedException extends RuntimeException {
    public VacancyURLMalformedException(String url) {
        super("Vacancy URL is malformed or could not be reached " + url);
    }

    public VacancyURLMalformedException(String url, int responseCode) {
        super("Vacancy URL is malformed or could not be reached " + url + " gave code " + responseCode);
    }
}


