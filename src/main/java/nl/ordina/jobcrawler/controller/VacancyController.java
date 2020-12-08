package nl.ordina.jobcrawler.controller;

import nl.ordina.jobcrawler.exception.VacancyNotFoundException;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.model.assembler.SkillModelAssembler;
import nl.ordina.jobcrawler.model.assembler.VacancyModelAssembler;
import nl.ordina.jobcrawler.payload.SearchRequest;
import nl.ordina.jobcrawler.payload.SearchResult;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.service.VacancyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/vacancies")
public class VacancyController {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final VacancyService vacancyService;
    private final VacancyModelAssembler vacancyModelAssembler;

    public VacancyController(VacancyService vacancyService, VacancyModelAssembler vacancyModelAssembler) {
        this.vacancyService = vacancyService;
        this.vacancyModelAssembler = vacancyModelAssembler;
    }

    /**
     *
     * @param value when entered vacancy results are filtered by this value and the skills are ignored
     * @param skills when entered vacancy results are filtered by the skills
     * @param page the current page number
     * @param size the size of te page
     *
     * @return vacancies from the database.
     */
    @GetMapping
    public ResponseEntity<SearchResult> getVacancies(@RequestParam(required = false) Optional<String> value,
                                                     @RequestParam(required = false) Optional<Set<String>> skills,
                                                     @RequestParam(required = false) Optional<String> location,
                                                     @RequestParam(required = false) Optional<Double> distance,
                                                     @RequestParam(required = false) Optional<String> fromDate,
                                                     @RequestParam(required = false) Optional<String> toDate,
                                                     @RequestParam(defaultValue = "desc") String dir,
                                                     @RequestParam(defaultValue = "postingDate") String sort,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        try {

            Pageable paging = PageRequest.of(page, size);

            SearchRequest searchRequest = new SearchRequest();
            value.ifPresent(searchRequest::setKeywords);
            fromDate.ifPresent(fd -> searchRequest.setFromDate(LocalDateTime.parse(fd, formatter)));
            toDate.ifPresent(td -> searchRequest.setToDate(LocalDateTime.parse(td, formatter)));
            skills.ifPresent(searchRequest::setSkills);

            location.ifPresent(searchRequest::setLocation);
            distance.ifPresent(searchRequest::setDistance);

            String[] sortingArray = new String[2];
            sortingArray[0] = sort;
            sortingArray[1] = dir;

            Page<VacancyDTO> vacancyDTOList = vacancyService.findByAnyValue(searchRequest, paging, sortingArray);

            if (vacancyDTOList.getContent().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            SearchResult searchResult = new SearchResult();
            searchResult.setVacancies(vacancyDTOList.getContent());
            searchResult.setCurrentPage(vacancyDTOList.getNumber());
            searchResult.setTotalItems(vacancyDTOList.getTotalElements());
            searchResult.setTotalPages(vacancyDTOList.getTotalPages());
            return new ResponseEntity<>(searchResult, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public CollectionModel<EntityModel<Vacancy>> getVacancies() {

        return vacancyModelAssembler.toCollectionModel(vacancyService.findAll());

    }

    /**
     * Creates a new vacancy.
     *
     * @param vacancy The vacancy to create.
     * @return The created vacancy and code 201 Created
     * Code 400 Bad Request if the given body is invalid
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<Vacancy>> createVacancy(@Valid @RequestBody Vacancy vacancy) {
        EntityModel<Vacancy> returnedVacancy = vacancyModelAssembler.toModel(vacancyService.save(vacancy));
        return ResponseEntity
                .created(returnedVacancy.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(returnedVacancy);
    }


    /**
     * Returns the vacancy with the specified ID.
     *
     * @param id The ID of the vacancy to retrieve.
     * @return The vacancy with the specified ID, or code 404 Not Found if the id was not found.
     * @throws VacancyNotFoundException when a vacancy is not found with the specified ID.
     */
    @GetMapping("/{id}")
    public EntityModel<Vacancy> getVacancy(@PathVariable UUID id) {
        Vacancy vacancy = vacancyService.findById(id)
                .orElseThrow(() -> new VacancyNotFoundException(id));
        return vacancyModelAssembler.toModel(vacancy);
    }

    /**
     * Returns the skills of a vacancy with the specified ID.
     *
     * @param id The ID of the vacancy.
     * @return The skills of the vacancy with the specified ID
     * @throws VacancyNotFoundException when a vacancy is not found with the specified ID.
     */
    @GetMapping("/{id}/skills")
    public CollectionModel<EntityModel<Skill>> getSkillsById(@PathVariable UUID id) {
        return new SkillModelAssembler().toCollectionModel(vacancyService.findSkillsByVacancyId(id));
    }

    /**
     * Deletes the vacancy with the specified ID.
     *
     * @param id The ID of the vacancy to delete.
     * @return A ResponseEntity with one of the following status codes:
     * 200 OK if the delete was successful
     * 404 Not Found if a vacancy with the specified ID is not found
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteVacancy(@PathVariable UUID id) {
        vacancyService.findById(id).orElseThrow(() -> new VacancyNotFoundException(id));
        vacancyService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
