package nl.ordina.jobcrawler.controller;

import nl.ordina.jobcrawler.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping("/coordinates")
public class CoordinatesController {

    private final LocationService locationService;

    @Autowired
    public CoordinatesController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public double[] getCoordinates(@RequestParam(required = true) String location) throws IOException {
        return locationService.getCoordinates(location);
    }

}
