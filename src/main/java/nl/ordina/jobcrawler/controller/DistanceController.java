package nl.ordina.jobcrawler.controller;

import nl.ordina.jobcrawler.exception.LocationNotFoundException;
import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.assembler.LocationModelAssembler;
import nl.ordina.jobcrawler.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/distance")
public class DistanceController {

    private final LocationService locationService;

    @Autowired
    public DistanceController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public double getDistance(@RequestParam(required = true) String from, @RequestParam(required = true) String to) {
        return locationService.getDistance(parseCoordinates(from),parseCoordinates(to));
    }

    public double[] parseCoordinates(String loc) {
        double[] coord = new double[2];
        if (loc.matches("\\d+(\\.\\d+)?,\\d+(\\.\\d+)?")) {
            coord[0] = Double.valueOf(loc.substring(0,loc.indexOf(',')));
            coord[1] = Double.valueOf(loc.substring(loc.indexOf(',')+1,loc.length()-1));
        } else {
            Location location = locationService.findByLocationName(loc).get();
            coord[0] = location.getLon();
            coord[1] = location.getLat();
        }
        return coord;
    }
}
