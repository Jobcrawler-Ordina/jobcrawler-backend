package nl.ordina.jobcrawler.repo;

import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID>, JpaSpecificationExecutor<Vacancy> {

    @Transactional
    Optional<Location> findByLocationName(String locationName);

 /*   @Query(value = "SELECT DISTINCT v.* FROM Vacancy AS v " +
            "WHERE lower(v.about) LIKE lower(concat('%', :value, '%')) OR lower(v.location) LIKE lower(concat('%', :value, '%')) " +
            "OR lower(v.title) LIKE lower(concat('%', :value, '%'))", nativeQuery = true)*/
    Optional<Location> findById(UUID id);

    void deleteById(UUID id);


}
