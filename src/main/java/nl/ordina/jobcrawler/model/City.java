package nl.ordina.jobcrawler.model;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
public class City {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    private String cityName;
    private double lon;
    private double lat;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="city_id",nullable=false)
    private Vacancy vacancy;
/*    @JoinTable(
            name = "city_vacancy",
            joinColumns = @JoinColumn(name = "vacancy_id"),
            inverseJoinColumns = @JoinColumn(name = "city_id"))*/

    public City(String cityName, double lon, double lat) {
        this.cityName = cityName;
        this.lon = lon;
        this.lat = lat;
    }

    public double getLon() { return lon; }
    public double getLat() { return lat; }
}
