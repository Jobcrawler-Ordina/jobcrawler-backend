package nl.ordina.jobcrawler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.collection.internal.PersistentBag;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private String locationName;
    private double lon;
    private double lat;
    private double distance;

/*    @OneToMany(mappedBy = "location", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    List<Vacancy> vacancies;*/

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

//    public PersistentBag getVacancies() { return (PersistentBag) vacancies; }
/*    public CopyOnWriteArrayList<Vacancy> getVacanciesAsCOWA() {
        PersistentBag vacanciesPB = getVacancies();
        CopyOnWriteArrayList<Vacancy> vacanciesAL = new CopyOnWriteArrayList<>();
        for (Vacancy vacancy : vacancies) {
            vacanciesAL.add(vacancy);
        }
        return vacanciesAL;
    }*/
//    public void setVacancies(List<Vacancy> vacancies) { this.vacancies = vacancies; }
/*    @Override
    public String toString() {
        String message;
        message = "Location{" +
                "id=" + id + '\'' +
                ", locationName='" + locationName + '\'' +
                ", lon=" + lon + '\'' +
                ", lat=" + lat + '\'' +
                ", vacancies_filled=" + !(vacancies==null);
        if(!(vacancies == null)) {
        message = message + ", vacancies_size=" + vacancies.size();
        }
        message = message + '}';
        return message;
    }*/
}
