package nl.ordina.jobcrawler.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString
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

/*    @OneToMany(fetch = FetchType.LAZY, mappedBy = "location")
    List<Vacancy> vacancies;*/

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "vacancy_location",
            joinColumns = @JoinColumn(name = "location_id"),
            inverseJoinColumns = @JoinColumn(name = "vacancy_id"))
    List<Vacancy> vacancies;

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

/*    @Override
    public String toString() {
        return "Location{" +
                "location_id=" + location_id +
                ", locationName='" + locationName + '\'' +
                ", lon=" + lon +
                ", lat=" + lat +
                ", vacancies=" + vacancies +
                '}';
    }*/
}
