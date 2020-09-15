package nl.ordina.jobcrawler.service;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.exception.VacancyURLMalformedException;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.repo.VacancyRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static nl.ordina.jobcrawler.repo.VacancySpecifications.findBySkill;
import static nl.ordina.jobcrawler.repo.VacancySpecifications.findByValue;

@Slf4j
@Service
public class VacancyService {

    private final VacancyRepository vacancyRepository;

    public VacancyService(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    /**
     * Returns the vacancy with the specified id.
     *
     * @param id ID of the vacancy to retrieve.
     * @return An optional of the requested vacancy if found, and an empty optional otherwise.
     */
    public Optional<Vacancy> findById(UUID id) {
        return vacancyRepository.findById(id);
    }

    public List<Vacancy> findAll() {
        return vacancyRepository.findAll();
    }
    public CopyOnWriteArrayList<Vacancy> findByLocationid(UUID id) {
        List<Vacancy> vacanciesList = vacancyRepository.findByLocation_Id(id);
        CopyOnWriteArrayList<Vacancy> vacanciesList2 = new CopyOnWriteArrayList<>();
        for(Vacancy vacancy: vacanciesList) {
            vacanciesList2.add(vacancy);
        }
        return vacanciesList2;
    }

    /**
     * Returns all vacancies in the database using pagination.
     *
     * @param paging - used for pagination
     * @return All vacancies in the database.
     */
    public Page<Vacancy> findAll(Pageable paging) {
        return vacancyRepository.findAll(paging);
    }

    /**
     * Returns all vacancies in the database filter by skills.
     *
     * @param skills - skills that needs to be filtered
     * @param paging - used for pagination
     * @return All vacancies in the database filtered by skills.
     */
    public Page<Vacancy> findBySkills(Set<String> skills, Pageable paging) {
        return vacancyRepository.findAll(findBySkill(skills), paging);
    }

    /**
     * Returns all vacancies in the database filter by any values that user enters in the search field.
     *
     * @param value  - value that needs to be filtered
     * @param paging - used for pagination
     * @return All vacancies in the database filter by any value.
     */
    public Page<Vacancy> findByAnyValue(String value, Pageable paging) {
        return vacancyRepository.findAll(findByValue(value), paging);
    }

    /**
     * Saves the specified vacancy to the database.
     *
     * @param vacancy The vacancy to save to the database.
     * @return The saved vacancy.
     * @throws VacancyURLMalformedException if the URL could not be reached.
     */
    public Vacancy save(Vacancy vacancy) {

        if (vacancy
                .hasValidURL()) {    //checking the url, if it is malformed it will throw a VacancyURLMalformedException
            return vacancyRepository.save(vacancy);
        } else {
            throw new VacancyURLMalformedException(vacancy.getVacancyURL());
        }
    }
    public void saveAll(List<Vacancy> vacancies) {
        vacancyRepository.saveAll(vacancies);
    }


    /**
     * Deletes the vacancy with the specified id.
     *
     * @param id The id of the vacancy to delete.
     * @return True if the operation was successful, false otherwise.
     */
    public boolean delete(UUID id) {

        try {
            vacancyRepository.deleteById(id);
            return true;
        } catch (DataAccessException e) {
            return false;
        }

    }
    public void deleteAll(List<Vacancy> vacancies) {
        UUID id;
        for(Vacancy vacancy : vacancies) {
            id = vacancy.getId();
            delete(id);
        }
    }

    /**
     * Returns the vacancy with the specified url.
     *
     * @param url url of the vacancy to retrieve.
     * @return An optional of the requested vacancy if found, and an empty optional otherwise.
     */
    public Optional<Vacancy> findByURL(String url) {
        return vacancyRepository.findByVacancyURLEquals(url);
    }
}
