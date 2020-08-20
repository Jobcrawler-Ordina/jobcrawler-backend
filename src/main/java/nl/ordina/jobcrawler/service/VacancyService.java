package nl.ordina.jobcrawler.service;

import nl.ordina.jobcrawler.controller.exception.VacancyURLMalformedException;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.repo.VacancyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class VacancyService implements CRUDService<Vacancy, UUID> {

    private final VacancyRepository vacancyRepository;

    @Autowired
    public VacancyService(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @Autowired
    private EntityManager entityManager;


    /**
     * Returns the vacancy with the specified id.
     *
     * @param id ID of the vacancy to retrieve.
     * @return An optional of the requested vacancy if found, and an empty optional otherwise.
     */
    @Override
    public Optional<Vacancy> findById(UUID id) {
        return vacancyRepository.findById(id);
    }

    @Override
    public List<Vacancy> findAll() {
        return vacancyRepository.findAll();
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
     * @return All vacancies in the database filter by skills.
     */
    public Page<Vacancy> findBySkills(Set<String> skills, Pageable paging) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Vacancy> criteriaQuery = criteriaBuilder.createQuery(Vacancy.class);
        Root<Vacancy> vacancyRoot = criteriaQuery.from(Vacancy.class);
        List<Predicate> predicatelist = new ArrayList<>();
        for (String s : skills) {
            predicatelist.add(criteriaBuilder.like(vacancyRoot.get("about"), "% " + s + " %"));
        }

        switch (predicatelist.size()) {
            case 2:
                criteriaQuery.where(criteriaBuilder.and(predicatelist.get(0), predicatelist.get(1)));
                break;

            case 3:
                criteriaQuery.where(criteriaBuilder.and(predicatelist.get(0), predicatelist.get(1), predicatelist.get(2)));
                break;

            case 4:
                criteriaQuery.where(criteriaBuilder.and(predicatelist.get(0), predicatelist.get(1), predicatelist.get(2), predicatelist.get(3)));
                break;

            case 5:
                criteriaQuery.where(criteriaBuilder.and(predicatelist.get(0), predicatelist.get(1), predicatelist.get(2), predicatelist.get(3), predicatelist.get(4)));
                break;
            default:
                criteriaQuery.where(criteriaBuilder.and(predicatelist.get(0)));

        }

        return new PageImpl<>(entityManager.createQuery(criteriaQuery).getResultList());


        //return vacancyRepository.findBySkills(skills,paging);
    }


    /**
     * Returns all vacancies in the database filter by any values that user enters in the search field.
     *
     * @param value  - value that needs to be filtered
     * @param paging - used for pagination
     * @return All vacancies in the database filter by any value.
     */
    public Page<Vacancy> findByAnyValue(String value, Pageable paging) {
        return vacancyRepository.findByAnyValue(value, paging);
    }

    /**
     * Updates the specified vacancy, identified by its id.
     *
     * @param vacancy The vacancy to update.
     * @return True if the update succeeded, otherwise false.
     */
    @Override
    public Vacancy update(UUID id, Vacancy vacancy) {
        return null;
    }


    /**
     * Saves the specified vacancy to the database.
     *
     * @param vacancy The vacancy to save to the database.
     * @return The saved vacancy.
     * @throws VacancyURLMalformedException if the URL could not be reached.
     */
    @Override
    public Vacancy save(Vacancy vacancy) {

        if (vacancy.hasValidURL()) {    //checking the url, if it is malformed it will throw a VacancyURLMalformedException
            return vacancyRepository.save(vacancy);
        } else {
            throw new VacancyURLMalformedException(vacancy.getVacancyURL());
        }

    }


    /**
     * Deletes the vacancy with the specified id.
     *
     * @param id The id of the vacancy to delete.
     * @return True if the operation was successful, false otherwise.
     */
    @Override
    public boolean delete(UUID id) {

        try {
            vacancyRepository.deleteById(id);
            return true;
        } catch (DataAccessException e) {
            return false;
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
