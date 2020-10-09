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

/*    @OneToMany(mappedBy = "location", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    List<Vacancy> vacancies;*/

    public Location(String locationName) {
        this.name = name;
    }

    public Location(String locationName, double lon, double lat) {
        this.name = name;
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
