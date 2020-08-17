package nl.ordina.jobcrawler.utils;

import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;

import java.util.HashSet;
import java.util.UUID;

public class SkillFactory {
    public static Skill create(String title) {
        return Skill.builder()
                .id(UUID.randomUUID())
                .name(title)
                .vacancies(new HashSet<Vacancy>())
                .build();
    }

}
