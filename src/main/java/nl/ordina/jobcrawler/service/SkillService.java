package nl.ordina.jobcrawler.service;

import nl.ordina.jobcrawler.exception.SkillNotFoundException;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.repo.SkillRepository;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/*
    This service operates on the skills in the database
    It contains functions that affect the skill table and the table that relates
    the skill set in the skill table with the vacancies.
 */


@Service
public class SkillService {

    private final SkillRepository skillRepository;

    @Autowired
    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public List<Skill> findByOrderByNameAsc() {
        return skillRepository.findByOrderByNameAsc();
    }

    /**
     * Returns the skill with the specified id.
     *
     * @param id ID of the skill to retrieve.
     * @return An optional of the requested skill if found, and an empty optional otherwise.
     */
    public Optional<Skill> findById(UUID id) {
        return skillRepository.findById(id);
    }

    /**
     * Returns all skills in the database.
     *
     * @return All skills in the database.
     */
    public List<Skill> findAll() {
        return skillRepository.findAll();
    }

    /**
     * Updates the a skill, identified by its id.
     *
     * @param newSkill The skill with the values to be updated.
     * @return True if the update succeeded, otherwise false.
     */
    public Skill update(UUID id, Skill newSkill) {

        return skillRepository.findById(id)
                .map(oldSkill -> {
                    oldSkill.setName(newSkill.getName());
                    return skillRepository.save(oldSkill);
                }).orElseThrow(() -> new SkillNotFoundException(id));
    }


    /**
     * Saves the specified skill to the database.
     *
     * @param skill The skill to save to the database.
     * @return The saved skill.
     */
    public Skill save(Skill skill) {
        return skillRepository.save(skill);
    }

    /**
     * Deletes the skill with the specified id.
     *
     * @param id The id of the skill to delete.
     */
    public void delete(UUID id) {
        skillRepository.deleteById(id);
    }

}
