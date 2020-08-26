package nl.ordina.jobcrawler.model;

import lombok.Data;

@Data
public class City {

    private final String cityName;
    private final double lon;
    private final double lat;

    public City(String cityName, double lon, double lat) {
        this.cityName = cityName;
        this.lon = lon;
        this.lat = lat;
    }

    public double getLon() { return lon; }
    public double getLat() { return lat; }
}
