package nl.ordina.jobcrawler.controller;

import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/coordinates")
public class CoordinatesController {

    private final LocationService locationService;

    @Autowired
    public CoordinatesController(LocationService locationService) {
        this.locationService = locationService;
    }

    /* Gets the coordinates of an entered location. Also saves the location to the DB*/
    @GetMapping
    public double[] getCoordinates(@RequestParam(required = true) String location) throws IOException {
        double[] coord;
        coord = locationService.getCoordinates(location);
        Optional<Location> existCheckLocation = locationService.findByLocationName(location);
        if (existCheckLocation.isEmpty()) {
            Location locationObj = new Location(location, coord);
            locationService.save(locationObj);
        }
        return coord;
    }

}
