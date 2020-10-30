package nl.ordina.jobcrawler.repo;

public class TestLocationService {
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
}
