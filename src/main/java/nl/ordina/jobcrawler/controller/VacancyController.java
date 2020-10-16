package nl.ordina.jobcrawler.controller;

import nl.ordina.jobcrawler.exception.VacancyNotFoundException;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.model.assembler.SkillModelAssembler;
import nl.ordina.jobcrawler.model.assembler.VacancyModelAssembler;
import nl.ordina.jobcrawler.payload.SearchResult;
import nl.ordina.jobcrawler.service.VacancyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/vacancies")
public class VacancyController {

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
                                                     @RequestParam(required = false) Optional<Long> distance,
                                                     @RequestParam(defaultValue = "desc") String dir,
                                                     @RequestParam(defaultValue = "postingDate") String sort,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        try {

            Sort sorting = dir.equals("desc") ? Sort.by(Sort.Direction.DESC, sort) : Sort.by(Sort.Direction.ASC, sort);
            Pageable paging = PageRequest.of(page, size, sorting);

            Page<Vacancy> vacancies = value.filter(v -> !v.isBlank()).map(v -> vacancyService.findByAnyValue(v, paging))
                    .orElse(skills.filter(s -> !s.isEmpty()).map(s -> vacancyService.findBySkills(s, paging))
                    .orElse(location.filter(l -> !l.isEmpty()).map(l -> vacancyService.findByLocationAndDistance(l, distance, paging))
                            .orElse(vacancyService.findAll(paging))));

            if (vacancies.getContent().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            SearchResult searchResult = new SearchResult();
            searchResult.setVacancies(vacancies.getContent());
            searchResult.setCurrentPage(vacancies.getNumber());
            searchResult.setTotalItems(vacancies.getTotalElements());
            searchResult.setTotalPages(vacancies.getTotalPages());
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
        Vacancy vacancy = vacancyService.findById(id)
                .orElseThrow(() -> new VacancyNotFoundException(id));
        return new SkillModelAssembler().toCollectionModel(vacancy.getSkills());
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
