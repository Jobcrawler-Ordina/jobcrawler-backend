package nl.ordina.jobcrawler.model;

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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "location", cascade = CascadeType.ALL)
    Set<Vacancy> vacancies;

/*    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "vacancy_location",
            joinColumns = @JoinColumn(name = "location_id"),
            inverseJoinColumns = @JoinColumn(name = "vacancy_id"))
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

/*    public PersistentBag getVacancies() {
        return (PersistentBag) vacancies;
    }

    public ArrayList<Vacancy> getVacanciesAsArrayList() {
        PersistentBag vacanciesPB = getVacancies();
        ArrayList<Vacancy> vacanciesAL = new ArrayList<>();
        for (Vacancy vacancy : vacancies) {
            vacanciesAL.add(vacancy);
        }
        return vacanciesAL;
    }

    public void setVacancies(ArrayList<Vacancy> vacancies) {
        this.vacancies = vacancies;
    }*/

    @Override
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
    }
}
