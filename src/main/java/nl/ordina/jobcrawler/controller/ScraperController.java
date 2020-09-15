package nl.ordina.jobcrawler.controller;

import nl.ordina.jobcrawler.service.ScraperService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping(path = "/scraper")
public class ScraperController {

    private final ScraperService scraperService;

    @Autowired
    public ScraperController(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    /**
     * start the scraping of jobs
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Async
    public ResponseEntity<Object> scrape() throws IOException, JSONException {
        scraperService.scrape();

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
    }
}
