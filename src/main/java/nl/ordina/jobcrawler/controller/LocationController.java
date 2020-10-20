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

import java.util.List;
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

    @GetMapping("/{locationIdOrName}")
    public EntityModel<Location> getLocationByIdOrName(@PathVariable String locationIdOrName) {
        if(locationIdOrName.matches(".*\\d.*")) {
            UUID id = UUID.fromString(locationIdOrName);
            return getLocationById(id);
        } else {
            return getLocationByName(locationIdOrName);
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

    @GetMapping
    public ResponseEntity<List<Location>> getLocations() {
        return new ResponseEntity<>(locationService.findByOrderByNameAsc(), HttpStatus.OK);
    }

}
