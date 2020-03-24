package nl.ordina.jobcrawler.controller;

import java.util.UUID;

public class VacancyNotFoundException extends RuntimeException {
    public VacancyNotFoundException(UUID id) {
        super("Could not find Job " + id);
    }
}
