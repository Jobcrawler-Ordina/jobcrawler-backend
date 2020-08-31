package nl.ordina.jobcrawler.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    private String locationName;
    private double lon;
    private double lat;

    @OneToMany(mappedBy = "location",cascade = CascadeType.ALL)
    private List<Vacancy> vacancies;

    /*    @JoinTable(
            name = "city_vacancy",
            joinColumns = @JoinColumn(name = "vacancy_id"),
            inverseJoinColumns = @JoinColumn(name = "city_id"))*/

    public Location(String locationName) {
        this.locationName = locationName;
    }

    public Location(String locationName, double lon, double lat) {
        this.locationName = locationName;
        this.lon = lon;
        this.lat = lat;
    }

    public double getLon() { return lon; }
    public double getLat() { return lat; }
}
