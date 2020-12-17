package nl.ordina.jobcrawler.controller;


import nl.ordina.jobcrawler.exception.SkillNotFoundException;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.assembler.SkillModelAssembler;
import nl.ordina.jobcrawler.payload.SkillDTO;
import nl.ordina.jobcrawler.service.SkillService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/skills")
public class SkillController {

    private final SkillService skillService;
    private final SkillModelAssembler skillModelAssembler;
    private final ModelMapper modelMapper;

    @Autowired
    public SkillController(SkillService skillService, SkillModelAssembler skillModelAssembler, ModelMapper modelMapper) {
        this.skillService = skillService;
        this.skillModelAssembler = skillModelAssembler;
        this.modelMapper = modelMapper;
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
     * @param skillDTO The skill to create.
     * @return The created skill and code 201 Created
     * Code 400 Bad Request if the given body is invalid
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<EntityModel<Skill>> createSkill(@Valid @RequestBody SkillDTO skillDTO) {
        Skill skill = modelMapper.map(skillDTO, Skill.class);

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<Skill>> updateSkill(@PathVariable UUID id, @Valid @RequestBody SkillDTO skillDTO) {
        skillService.findById(id).orElseThrow(() -> new SkillNotFoundException(id));
        Skill skill = modelMapper.map(skillDTO, Skill.class);
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteSkill(@PathVariable UUID id) {
        skillService.findById(id).orElseThrow(() -> new SkillNotFoundException(id));
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }


}

