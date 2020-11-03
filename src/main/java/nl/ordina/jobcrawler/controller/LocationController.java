package nl.ordina.jobcrawler.controller;

import nl.ordina.jobcrawler.exception.LocationNotFoundException;
import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.assembler.LocationModelAssembler;
import nl.ordina.jobcrawler.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;
    private final LocationModelAssembler locationModelAssembler;

    @Autowired
    public LocationController(LocationService locationService, LocationModelAssembler locationModelAssembler) {
        this.locationService = locationService;
        this.locationModelAssembler = locationModelAssembler;
    }

    @GetMapping("/coordinates")
    public ResponseEntity<Map<String, Serializable>> getLocationByCoordinates(
            @RequestParam Optional<Double> lat,
            @RequestParam Optional<Double> lon) throws IOException {
        String location = "";
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        boolean result = false;
        if (lat.isPresent() && lon.isPresent()) {
            location = locationService.getLocation(lat.get(), lon.get());
            httpStatus = HttpStatus.OK;
            result = true;
        }
        return ResponseEntity.status(httpStatus).body(Map.of("success", result, "location", location));
    }

    @GetMapping
    public ResponseEntity<List<Location>> getLocations() {
        return new ResponseEntity<>(locationService.findByOrderByNameAsc(), HttpStatus.OK);
    }

    @GetMapping("/{locIdOrName}")
    public EntityModel<Location> getLocationByIdOrName(@PathVariable String locIdOrName) {
        if(locIdOrName.matches(".*\\d.*")) {
            UUID id = UUID.fromString(locIdOrName);
            return getLocationById(id);
        } else {
            return getLocationByName(locIdOrName);
        }
    }

    private EntityModel<Location> getLocationById(UUID id) {
        Location location = locationService.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));
        return locationModelAssembler.toModel(location);
    }

    private EntityModel<Location> getLocationByName(String locationName) {
        Location location = locationService.findByLocationName(locationName)
                .orElseThrow(() -> new LocationNotFoundException(locationName));
        return locationModelAssembler.toModel(location);
    }

    @GetMapping("coordinates/{location}")
    public double[] getCoordinates(@PathVariable String location) throws IOException {
        double[] coord;
        coord = locationService.getCoordinates(location);
        Optional<Location> existCheckLocation = locationService.findByLocationName(location);
        if (existCheckLocation.isEmpty()) {
            Location locationObj = new Location(location, coord);
            locationService.save(locationObj);
        }
        return coord;
    }

    @GetMapping("/distance")
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
            coord[0] = location.getLat();
            coord[1] = location.getLon();
        }
        return coord;
    }
}
