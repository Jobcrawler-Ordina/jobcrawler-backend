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
    private double lat;
    private double lon;

    public Location(String name) {
        this.name = name;
    }

    public Location(String name, double[] coord) {
        this(name, coord[0], coord[1]);
    }

    public Location(String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public double[] getCoord() {return new double[]{this.lat, this.lon}; }
}
