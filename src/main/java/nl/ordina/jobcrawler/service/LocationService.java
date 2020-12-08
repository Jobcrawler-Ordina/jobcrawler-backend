package nl.ordina.jobcrawler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.exception.LocationNotFoundException;
import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.payload.opensearch.Coordinates;
import nl.ordina.jobcrawler.payload.opensearch.Place;
import nl.ordina.jobcrawler.repo.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class LocationService {

    private static final String API_KEY = "Xd5hXSuQvqUJJbJh3iacOXZAcskvP7gI";
    private static final String EMPTY_API_RESPONSE = "[]";
    private static final String ERROR_API_RESPONSE = "{\"error\":\"Unable to geocode\"}";

    private static final String GET_COORD_URL = "http://open.mapquestapi.com/nominatim/v1/search.php?format=json&key={key}&q={location}&addressdetails=0&limit=1&countrycodes=NL";
    private static final String GET_LOCNAME_URL = "http://open.mapquestapi.com/nominatim/v1/reverse.php?format=json&key={key}&lat={lat}&lon={lon}";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.locationRepository = locationRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Location> findByOrderByNameAsc() {
        return locationRepository.findByOrderByNameAsc();
    }

    public Optional<Location> findById(UUID id) {
        return locationRepository.findById(id);
    }

    public Optional<Location> findByLocationName(String locationName) {return locationRepository.findByName(locationName); }

    public Optional<Location> findByLocationNameInVacancyList(String locationName, List<Vacancy> allVacancies) {
        for(Vacancy vacancy : allVacancies) {
            if(vacancy.getLocation() != null && vacancy.getLocation().getName().equals(locationName)) {
                    return Optional.of(vacancy.getLocation());
            }
        }
        return Optional.empty();
    }

    public String getLocation(double lat, double lon) throws IOException {
        String jsonResponse = restTemplate.getForObject(GET_LOCNAME_URL, String.class, API_KEY, lat, lon);
        if (!ERROR_API_RESPONSE.equals(jsonResponse)) {
            Place place = objectMapper.readValue(jsonResponse, Place.class);
            if (!ObjectUtils.isEmpty(place.getAddress().getTown()) ) {
                return place.getAddress().getTown();
            }
            else if (!ObjectUtils.isEmpty(place.getAddress().getCity())) {
                return place.getAddress().getCity();
            }
            return "";
        }
        return "";
    }

    public double[] getCoordinates(String location) throws IOException {
        double[] coord = new double[2];
        String jsonResponse = restTemplate.getForObject(GET_COORD_URL, String.class, API_KEY, location);
        if (!ObjectUtils.isEmpty(jsonResponse) && !EMPTY_API_RESPONSE.equals(jsonResponse) && StringUtils.hasText(jsonResponse)) {
            jsonResponse = jsonResponse.substring(1, jsonResponse.length() - 1);
            Coordinates openSearchCoordinates = objectMapper.readValue(jsonResponse, Coordinates.class);
            coord[0] = openSearchCoordinates.getLat();
            coord[1] = openSearchCoordinates.getLon();
        } else {
            throw new LocationNotFoundException(location);
        }
        return coord;
    }

    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    public Location save(Location location) {
        return locationRepository.save(location);
    }

    public boolean delete(UUID id) {
        locationRepository.deleteById(id);
        return false;
    }
}
