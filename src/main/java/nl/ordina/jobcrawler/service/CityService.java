package nl.ordina.jobcrawler.service;

import nl.ordina.jobcrawler.model.City;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CityService {

/*  public static void main(String[] args) throws IOException, JSONException {
        final City c1 = getCoordinates("Amsterdam");
        final City c2 = getCoordinates("Woerden");

        double distance = distance(c1.getLon(), c1.getLat(), c2.getLon(), c2.getLat());
        System.out.println("\nDistance between " + c1.getCity() + " and " + c2.getCity() + " is: " + String.format("%.3f", distance) + "km.");
    }   */

    public static City getCoordinates(final String city) throws IOException, JSONException {
        final String apiKey = "Xd5hXSuQvqUJJbJh3iacOXZAcskvP7gI";
        final String url = "http://open.mapquestapi.com/nominatim/v1/search.php?key=" + apiKey + "&format=json&q=" + city + "&addressdetails=1&limit=1";

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

            return new City(city, jsonResponse.getDouble("lon"), jsonResponse.getDouble("lat"));

        }
        return new City(city, 0, 0);
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

}
