package nl.ordina.jobcrawler.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(UUID id) {
        super("Could not find location with id: " + id);
    }
    public LocationNotFoundException(String locationName) {
        super("Could not find location with name: " + locationName);
    }
}
