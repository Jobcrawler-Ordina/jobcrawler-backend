package nl.ordina.jobcrawler.repo;

import nl.ordina.jobcrawler.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    Optional<Location> findByName(String name);

    Optional<Location> findById(UUID id);

    List<Location> findByOrderByNameAsc();

    void deleteById(UUID id);

}
