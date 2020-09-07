package nl.ordina.jobcrawler.repo;

import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Vacancy;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    @Transactional
    Optional<Location> findByLocationName(String locationName);

 /*   @Query(value = "SELECT DISTINCT v.* FROM Vacancy AS v " +
            "WHERE lower(v.about) LIKE lower(concat('%', :value, '%')) OR lower(v.location) LIKE lower(concat('%', :value, '%')) " +
            "OR lower(v.title) LIKE lower(concat('%', :value, '%'))", nativeQuery = true)*/
    @Query("SELECT l FROM Location l JOIN FETCH l.vacancies")
    Optional<Location> findById(UUID id);

}
