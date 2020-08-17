package nl.ordina.jobcrawler.repo;

import nl.ordina.jobcrawler.model.Vacancy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, UUID> {

    @Transactional
    Optional<Vacancy> findByVacancyURLEquals(String url);

    /**
     * This query will fetch the details of Vacancies filtered by skills along with the pagination parameters.
     * @param skills - set iof skills that will be used to filter
     * @param pageable - pagination parameters
     * @return filtered vacancies
     */

    @Query(value = "SELECT DISTINCT v.* FROM Vacancy AS v " +
            "JOIN vacancy_skills AS vs ON vs.vacancy_id = v.id " +
            "JOIN skill AS s ON s.id = vs.skill_id WHERE s.name IN (:skills)" , nativeQuery = true)
    Page<Vacancy> findBySkills(@Param("skills") Set<String> skills, Pageable pageable);


    /**
     * This query will filtered the vacancies by any serach value that used will entered in the search field.
     * @param value - search value
     * @param pageable - pagaination parameteres
     * @return - filtered vacancies
     */

    @Query(value = "SELECT DISTINCT v.* FROM Vacancy AS v " +
            "WHERE lower(v.about) LIKE lower(concat('%', :value, '%')) OR lower(v.location) LIKE lower(concat('%', :value, '%')) " +
            "OR lower(v.title) LIKE lower(concat('%', :value, '%'))", nativeQuery = true)
    Page<Vacancy> findByAnyValue(@Param("value") String value, Pageable pageable);



}
