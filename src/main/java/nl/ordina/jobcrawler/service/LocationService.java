package nl.ordina.jobcrawler.service;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.model.Location;

import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.repo.LocationRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
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
public class LocationService implements CRUDService<Location, UUID> {

    private final LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public Optional<Location> findById(UUID id) {
        return locationRepository.findById(id);
    }

    public Optional<Location> findByLocationName(String locationName) {return locationRepository.findByLocationName(locationName); }

    public static Location getCoordinates(String location) throws IOException, JSONException {
        final String apiKey = "Xd5hXSuQvqUJJbJh3iacOXZAcskvP7gI";
        String location2 = location.replace(" ","%20");
        final String url = "http://open.mapquestapi.com/nominatim/v1/search.php?key=" + apiKey + "&format=json&q=" + location2 + "&addressdetails=1&limit=1";

        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        connection.setRequestMethod("GET");
        //add request header
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (connection.getResponseCode() == 200) {
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + connection.getResponseCode());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            String response = in.readLine();
            response = response.substring(1, response.length() - 1);
            System.out.println(response);
            in.close();
            //Read JSON response and return
            JSONObject jsonResponse = new JSONObject(response);

            return new Location(location, jsonResponse.getDouble("lon"), jsonResponse.getDouble("lat"));

        }
        return new Location(location, 0, 0);
    }

    private static double distance(double lon1,
                                   double lat1,
                                   double lon2,
                                   double lat2) {

        System.out.println(Math.sqrt(Math.pow((lon2 - lon1), 2) + Math.pow((lat2 - lat1), 2)) * 100);

        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;


        // calculate the result
        return (c * r);


    }

    @Override
    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    @Override
    public Location update(UUID uuid, Location location) {
        return null;
    }

    @Override
    public Location save(Location location) {
        return locationRepository.save(location);
    }

    @Override
    public boolean delete(UUID id) {
        return false;
    }
}
