package nl.ordina.jobcrawler.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Getter
@ToString
@Setter
@Entity
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    private String name;
    private double lon;
    private double lat;

    public Location(String name) {
        this.name = name;
    }

    public Location(String name, double[] coord) {
        this(name, coord[0], coord[1]);
    }

    public Location(String name, double lon, double lat) {
        this.name = name;
        this.lon = lon;
        this.lat = lat;
    }

    public double[] getCoord() {return new double[]{this.lon, this.lat}; }
}
