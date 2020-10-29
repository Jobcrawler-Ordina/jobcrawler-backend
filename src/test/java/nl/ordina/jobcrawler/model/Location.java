package nl.ordina.jobcrawler.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Getter
@ToString
@Setter
@Entity
@NoArgsConstructor
//@NamedNativeQuery(
//        name = "getDistance",
//        callable = true,
//        query = "{call GETDISANCE(?, ?, ?, ?)}",
//        resultClass = Review.class)
public class Location {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "uuid-char")
    private UUID id;
    private String name;
    private double lon;
    private double lat;

    @Formula(value = "getDistance(lat,lon,51.7832704,4.908646399999999)")
    private int distToLoc;

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
