package nl.ordina.jobcrawler.repo;

import nl.ordina.jobcrawler.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    Optional<Location> findByLocationName(String locationName);

    Optional<Location> findById(UUID id);

    void deleteById(UUID id);

}
