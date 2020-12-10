package nl.ordina.jobcrawler.utils;

import nl.ordina.jobcrawler.model.Vacancy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class MockData {

    public static Vacancy mockVacancy(String title) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return Vacancy.builder()
                .id(UUID.randomUUID())
                .vacancyURL("example.com")
                .title(title)
                .broker("broker")
                .vacancyNumber("1")
                .hours(30)
                .postingDate(LocalDateTime.parse("2020-04-14 00:00", formatter))
                .about("The applicant should be fluent in Java")
                .build();
    }

}
