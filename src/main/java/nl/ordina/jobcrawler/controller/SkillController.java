package nl.ordina.jobcrawler.controller;


import nl.ordina.jobcrawler.controller.exception.SkillNotFoundException;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.assembler.SkillModelAssembler;
import nl.ordina.jobcrawler.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@CrossOrigin
@RestController
@RequestMapping("/skills")
public class SkillController {

    private final SkillService skillService;
    private final SkillModelAssembler skillModelAssembler;

    @Autowired
    public SkillController(SkillService skillService, SkillModelAssembler skillModelAssembler) {
        this.skillService = skillService;
        this.skillModelAssembler = skillModelAssembler;
    }

    /**
     * Returns all skills in the database.
     *
     * @return All skills in the database.
     */
    @GetMapping
    public CollectionModel<EntityModel<Skill>> getSkills() {

        return skillModelAssembler.toCollectionModel(skillService.findByOrderByNameAsc());
    }

    /**
     * Creates a new skill.
     *
     * @param skill The skill to create.
     * @return The created skill and code 201 Created
     * Code 400 Bad Request if the given body is invalid
     */
    @PostMapping
    public ResponseEntity<EntityModel<Skill>> createSkill(@Valid @RequestBody Skill skill) {

        EntityModel<Skill> returnedSkill = skillModelAssembler.toModel(skillService.save(skill));
        return ResponseEntity
                .created(returnedSkill.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(returnedSkill);

    }


    /**
     * Returns the skill with the specified ID.
     *
     * @param id The ID of the skill to retrieve.
     * @return The skill with the specified ID, or code 404 Not Found if the id was not found.
     * @throws SkillNotFoundException when a skill is not found with the specified ID.
     */
    @GetMapping("/{id}")
    public EntityModel<Skill> getSkill(@PathVariable UUID id) {
        Skill skill = skillService.findById(id)
                .orElseThrow(() -> new SkillNotFoundException(id));
        return skillModelAssembler.toModel(skill);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Skill>> updateSkill(@PathVariable UUID id, @Valid @RequestBody Skill skill) {
        skillService.findById(id).orElseThrow(() -> new SkillNotFoundException(id));
        EntityModel<Skill> updatedSkillEntityModel = skillModelAssembler.toModel(skillService.update(id, skill));

        return ResponseEntity
                .created(updatedSkillEntityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(updatedSkillEntityModel);
    }

    /**
     * Deletes the skill with the specified ID.
     *
     * @param id The ID of the skill to delete.
     * @return A ResponseEntity with one of the following status codes:
     * 200 OK if the delete was successful
     * 404 Not Found if a skill with the specified ID is not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSkill(@PathVariable UUID id) {
        skillService.findById(id).orElseThrow(() -> new SkillNotFoundException(id));
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }


}

