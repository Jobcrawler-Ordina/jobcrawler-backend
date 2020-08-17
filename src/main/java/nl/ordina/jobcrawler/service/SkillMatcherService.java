package nl.ordina.jobcrawler.service;


import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
*  This service contains the functionality to match skills in the skill table with
*  vacancies in the vacancy table and to add or update relationship records accordingly:
*  It relies on the SkillService to interact with the database.
*
* */

@Slf4j
@Service
public class SkillMatcherService {


    private final SkillService skillService;
    private final VacancyService vacancyService;


    @Autowired
    public SkillMatcherService(
            SkillService skillService,
            VacancyService vacancyService) {
        this.skillService = skillService;
        this.vacancyService = vacancyService;
    }

    //         checks whether the vacancy text matches the skill name
    public boolean matchesSkill(String skillName, Vacancy vacancy) {
        return (vacancy.getAbout().toUpperCase().contains(skillName.toUpperCase()));
    }

    //     Change the match with skills for an existing vacancy in the database
    //     removes previous relationships with the skill table from this vacancy
    //     adds relationships to the skill table for which a matchesSkill holds
    public Set<Skill> findMatchingSkills(Vacancy vacancy) {
        List<Skill> skills = skillService.findAll();
        Set<Skill> matchedSkills = new HashSet<>();

        for (Skill s: skills) {
            if (matchesSkill(s.getName(), vacancy)) {
                matchedSkills.add(s);
            }
        }
        log.info(vacancy.getTitle() + " matched with skills: " + matchedSkills.toString());
        return matchedSkills;//vacancy.setSkills(matchedSkills);
    } 

    // Rematch all vacancies with the skills in the skill table
    public void relinkSkills() {
            skillService.deleteReferencesToSkills();
            List<Vacancy> vacancies = vacancyService.findAll();
            for (Vacancy vacancy: vacancies) {
                Set<Skill> matchingSkills = findMatchingSkills(vacancy);
                // not very fast -- could be improved
                if (matchingSkills.size() > 0) {
                    skillService.createMatchingSkillLinks(vacancy, matchingSkills);
                }
            }
    }

}
