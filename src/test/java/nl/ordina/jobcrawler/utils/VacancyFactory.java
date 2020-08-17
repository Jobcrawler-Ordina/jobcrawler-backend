package nl.ordina.jobcrawler.utils;

import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;

import java.util.HashSet;
import java.util.UUID;

public class VacancyFactory {
    public static Vacancy create(String title) {
        return Vacancy.builder()
                .id(UUID.randomUUID())
                .vacancyURL("example.com")
                .title(title)
                .broker("broker")
                .vacancyNumber("1")
                .hours("30")
                .location("location example")
                .postingDate("14 April 2020")
                .about("this is a description of the example job")
                .skills(new HashSet<Skill>())
                .build();
    }
}
