package nl.ordina.jobcrawler.service;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.repo.LocationRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class LocationService {

    private static final String API_KEY = "Xd5hXSuQvqUJJbJh3iacOXZAcskvP7gI";
    private static final String EMPTY_API_RESPONSE = "[]";
    
    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
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

    public static double[] getCoordinates(String location) throws IOException, JSONException {
        double[] coord = new double[2];
        String location2 = location.concat(", Nederland");
        location2 = location2.replace(" ","%20");
        final String url = "http://open.mapquestapi.com/nominatim/v1/search.php?key=" + API_KEY + "&format=json&q=" + location2 + "&addressdetails=1&limit=1";

        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        connection.setRequestMethod("GET");
        //add request header
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (connection.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = in.readLine();
            if (!EMPTY_API_RESPONSE.equals(response)) {
                response = response.substring(1, response.length() - 1);
                log.debug(response);
                in.close();
                //Read JSON response and return
                JSONObject jsonResponse = new JSONObject(response);

                coord[0] = jsonResponse.getDouble("lon");
                coord[1] = jsonResponse.getDouble("lat");
            }
            return coord;
        }
        return coord;
    }

    public String getLocation(double lat, double lon) throws IOException, JSONException {
        final String url = "http://open.mapquestapi.com/nominatim/v1/reverse.php?key=" + API_KEY + "&format=json&lat=" + lat + "&lon=" + lon;
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        connection.setRequestMethod("GET");
        //add request header
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (connection.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = in.readLine();
            if (!EMPTY_API_RESPONSE.equals(response)) {            
                log.debug(response);
                in.close();
                //Read JSON response and return
                JSONObject jsonResponse = new JSONObject(response);
                JSONObject jsonAddress = jsonResponse.getJSONObject("address");
                return jsonAddress.has("town") ? jsonAddress.getString("town") : jsonAddress.has("city") ? jsonAddress.getString("city") : "";
            }
        }
        return "";
    }

    public static double getDistance(double[] coord1, double[] coord2) {
        return getDistance(coord1[0], coord1[1], coord2[0], coord2[1]);
    }
    public static double getDistance(double[] coord1, double lon2, double lat2) {
        return getDistance(coord1[0], coord1[1], lon2, lat2);
    }
    public static double getDistance(double lon1, double lat1, double[] coord2) {
        return getDistance(lon1, lat1, coord2[0], coord2[1]);
    }

    public static double getDistance(double lon1, double lat1, double lon2, double lat2) {
        // Convert degrees to radians
        double dlon1 = Math.toRadians(lon1);
        double dlat1 = Math.toRadians(lat1);
        double dlon2 = Math.toRadians(lon2);
        double dlat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = dlon2 - dlon1;
        double dlat = dlat2 - dlat1;
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
