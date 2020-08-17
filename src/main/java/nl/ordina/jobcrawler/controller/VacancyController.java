package nl.ordina.jobcrawler.controller;

import nl.ordina.jobcrawler.SearchResult;
import nl.ordina.jobcrawler.controller.exception.VacancyNotFoundException;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.model.assembler.SkillModelAssembler;
import nl.ordina.jobcrawler.model.assembler.VacancyModelAssembler;
import nl.ordina.jobcrawler.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@CrossOrigin
@RestController
@RequestMapping("/vacancies")
public class VacancyController {

    private final VacancyService vacancyService;
    private final VacancyModelAssembler vacancyModelAssembler;

    @Autowired
    public VacancyController(VacancyService vacancyService, VacancyModelAssembler vacancyModelAssembler) {
        this.vacancyService = vacancyService;
        this.vacancyModelAssembler = vacancyModelAssembler;
    }

    /**
     * Returns all vacancies in the database.
     *
     * @return All vacancies in the database.
     */
    @GetMapping
    public ResponseEntity<SearchResult> getVacancies(
            @RequestParam(required = false) String value,
            @RequestParam(required = false) Set<String> skills,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Vacancy> vacancyList = new ArrayList<>();
            Pageable paging = PageRequest.of(page, size);

            Page<Vacancy> vacancies;

            if (value != null && !value.isBlank())
                vacancies = vacancyService.findByAnyValue(value, paging);
            else if(skills != null && !skills.isEmpty())
                vacancies = vacancyService.findBySkills(skills, paging);
            else
                vacancies = vacancyService.findAll(paging);
            vacancyList = vacancies.getContent();

            if (vacancyList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            SearchResult searchResult = new SearchResult();
            searchResult.setVacancies(vacancyList);
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
    public ResponseEntity<?> deleteVacancy(@PathVariable UUID id) {
        vacancyService.findById(id).orElseThrow(() -> new VacancyNotFoundException(id));
        vacancyService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
