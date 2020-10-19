package nl.ordina.jobcrawler.service;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.exception.VacancyURLMalformedException;
import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.repo.VacancyRepository;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static nl.ordina.jobcrawler.repo.VacancySpecifications.*;

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

    public Page<Vacancy> findByLocationAndDistance(String loc, Optional<Long> dist, Boolean showEmptyLoc, Pageable paging) {
        if((!dist.isPresent())||(dist.get()==0)) {
            return vacancyRepository.findAll(findByLocName(loc), paging);
        } else {
            double[] coord;
            try {
                coord = LocationService.getCoordinates(loc);
                List<Vacancy> vacancies = vacancyRepository.findAll(findByDistance(coord,dist.get()));
                if (showEmptyLoc) {
                    vacancies.addAll(vacancyRepository.findAll(findWithoutLocation()));
                }
                int ps = paging.getPageSize();
                int pn = paging.getPageNumber();
                PageImpl<Vacancy> p = new PageImpl<>(vacancies.subList(ps*pn,Math.min((pn+1)*ps, vacancies.size())), paging, vacancies.size());
                return p;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Returns all vacancies in the database using pagination.
     *
     * @param paging - used for pagination
     * @return All vacancies in the database.
     */
    public Page<Vacancy> findAll(Boolean emptyLocs,Pageable paging) {
        Page<Vacancy> v;
        if(emptyLocs) {
            v = vacancyRepository.findAll(paging);
        } else {
            v = vacancyRepository.findAll(findWithLocation(),paging);
        }
        return v;
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

    public static List<Vacancy> convertVacancyDTOs(List<VacancyDTO> vacancyDTOs) {
        List<Vacancy> vacancies = new CopyOnWriteArrayList<>();
        Vacancy vacancy;
        for(VacancyDTO vacancyDTO : vacancyDTOs) {
            vacancy = convertVacancyDTO(vacancyDTO);
            vacancies.add(vacancy);
        }
        return vacancies;
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
