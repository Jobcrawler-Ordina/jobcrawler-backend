package nl.ordina.jobcrawler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.payload.opensearch.Coordinates;
import nl.ordina.jobcrawler.payload.opensearch.Place;
import nl.ordina.jobcrawler.repo.LocationRepository;
import org.json.JSONException;
import org.springframework.stereotype.Service;
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
            if(!(vacancy.getLocation()==null)) {
                if (vacancy.getLocation().getName().equals(locationName)) {
                    return Optional.of(vacancy.getLocation());
                }
            }
        }
        return Optional.empty();
    }

    public String getLocation(double lat, double lon) throws IOException, JSONException {
        String jsonResponse = restTemplate.getForObject(GET_LOCNAME_URL, String.class, API_KEY, lat, lon);
        if (!ERROR_API_RESPONSE.equals(jsonResponse)) {
            Place place = objectMapper.readValue(jsonResponse, Place.class);
            return !StringUtils.isEmpty(place.getAddress().getTown()) ? place.getAddress().getTown() : !StringUtils
                    .isEmpty(place.getAddress().getCity()) ? place.getAddress().getCity() : "";
        }
        return "";
    }

    public double[] getCoordinates(String location) throws IOException {
        double[] coord = new double[2];
        String jsonResponse = restTemplate.getForObject(GET_COORD_URL, String.class, API_KEY, location);
        if (!StringUtils.isEmpty(jsonResponse) && !EMPTY_API_RESPONSE.equals(jsonResponse)) {
            jsonResponse = jsonResponse.substring(1, jsonResponse.length() - 1);
            Coordinates openSearchCoordinates = objectMapper.readValue(jsonResponse, Coordinates.class);
            coord[0] = openSearchCoordinates.getLat();
            coord[1] = openSearchCoordinates.getLon();
        }
        return coord;
    }

    public static double getDistance(double[] coord1, double[] coord2) {
        return getDistance(coord1[0], coord1[1], coord2[0], coord2[1]);
    }
    public static double getDistance(double[] coord1, double lat2, double lon2) {
        return getDistance(coord1[0], coord1[1], lat2, lon2);
    }
    public static double getDistance(double lat1, double lon1, double[] coord2) {
        return getDistance(lat1, lon1, coord2[0], coord2[1]);
    }

    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert degrees to radians
        double dlat1 = Math.toRadians(lat1);
        double dlon1 = Math.toRadians(lon1);
        double dlat2 = Math.toRadians(lat2);
        double dlon2 = Math.toRadians(lon2);

        // Haversine formula
        double dlat = dlat2 - dlat1;
        double dlon = dlon2 - dlon1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(dlat1) * Math.cos(dlat2)
                * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        // Radius of earth in kilometers. Use 3956 for miles
        double r = 6371;
        // calculate the result
        return (c * r);
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
