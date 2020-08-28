package nl.ordina.jobcrawler.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "vacancy_location")
public class VacancyLocation {

    @Id
    private Long VacancyId;

    private List<City> cities;

    @OneToMany(targetEntity = Vacancy.class,mappedBy = "vacancyLocation",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<City> getCities() {return cities};

    public void setCities(List<City> cities) {
        this.cities = cities;
    }

    public Long getVacancyId() {
        return VacancyId;
    }

    public void setVacancyId(Long vacancyId) {
        VacancyId = vacancyId;
    }

}
