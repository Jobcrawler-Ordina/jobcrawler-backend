package nl.ordina.jobcrawler.service;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.exception.VacancyURLMalformedException;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.payload.SearchRequest;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.repo.VacancyRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static nl.ordina.jobcrawler.repo.VacancySpecifications.vacancySearch;

@Slf4j
@Service
public class VacancyService {

    private final VacancyRepository vacancyRepository;
    private final LocationService locationService;

    public VacancyService(VacancyRepository vacancyRepository, LocationService locationService) {
        this.vacancyRepository = vacancyRepository;
        this.locationService = locationService;
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

    /**
     * Returns all vacancies in the database filter by any values that user enters in the search field.
     *
     * @param searchRequest  - values that need to be filtered
     * @param paging - used for pagination
     * @return All vacancies in the database filter by any value.
     */
    public Page<Vacancy> findByAnyValue(SearchRequest searchRequest, Pageable paging) {

        if (!StringUtils.isEmpty(searchRequest.getLocation())) {
            try {
                searchRequest.setCoord(locationService.getCoordinates(searchRequest.getLocation()));
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        Page<Vacancy> p = vacancyRepository.findAll(vacancySearch(searchRequest), paging);
        return p;
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

    /**
     * Returns the vacancy with the specified url.
     *
     * @param url url of the vacancy to retrieve.
     * @return An optional of the requested vacancy if found, and an empty optional otherwise.
     */
    public Optional<Vacancy> findByURL(String url) {
        return vacancyRepository.findByVacancyURLEquals(url);
    }

    public static List<Vacancy> convertVacancyDTOs(List<VacancyDTO> vacancyDTOs) {
        return vacancyDTOs.stream().map(VacancyService::convertVacancyDTO).collect(Collectors.toList());
    }

    public static Vacancy convertVacancyDTO(VacancyDTO vacancyDTO) {
        return Vacancy.builder()
                .vacancyURL(vacancyDTO.getVacancyURL())
                .title(vacancyDTO.getTitle())
                .broker(vacancyDTO.getBroker())
                .vacancyNumber(vacancyDTO.getVacancyNumber())
                .hours(vacancyDTO.getHours())
                .salary(vacancyDTO.getSalary())
                .postingDate(vacancyDTO.getPostingDate())
                .about(vacancyDTO.getAbout())
                .company(vacancyDTO.getCompany())
                .build();
    }
}
