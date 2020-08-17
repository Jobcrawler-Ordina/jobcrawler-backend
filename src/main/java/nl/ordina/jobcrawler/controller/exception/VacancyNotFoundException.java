package nl.ordina.jobcrawler.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VacancyNotFoundException extends RuntimeException {
    public VacancyNotFoundException(UUID id) {
        super("Could not find vacancy with id: " + id);
    }
}
