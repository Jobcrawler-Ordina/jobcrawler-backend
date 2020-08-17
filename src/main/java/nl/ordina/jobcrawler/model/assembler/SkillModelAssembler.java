package nl.ordina.jobcrawler.model.assembler;

import nl.ordina.jobcrawler.controller.SkillController;
import nl.ordina.jobcrawler.model.Skill;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SkillModelAssembler implements RepresentationModelAssembler<Skill, EntityModel<Skill>> {
    /**
     * Takes a skill and adds RESTful links to it and returns it as an EntityModel
     * @param skill skill to be converted
     * @return EntityModel of the skill containing links
     */
    @Override
    public EntityModel<Skill> toModel(Skill skill) {
        return EntityModel.of(skill,
                linkTo(methodOn(SkillController.class).getSkill(skill.getId())).withSelfRel(),
                linkTo(methodOn(SkillController.class).getSkills()).withRel("skills")
        );
    }

    @Override
    public CollectionModel<EntityModel<Skill>> toCollectionModel(Iterable<? extends Skill> skills) {
        List<EntityModel<Skill>> returnSkills = new ArrayList<>();
        skills.forEach(skill -> returnSkills.add(toModel(skill)));

        return CollectionModel.of(returnSkills,
                linkTo(methodOn(SkillController.class).getSkills()).withSelfRel()
        );

    }
}
