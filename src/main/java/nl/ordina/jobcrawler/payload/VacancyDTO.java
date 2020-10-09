package nl.ordina.jobcrawler.payload;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
public class VacancyDTO {

    private String vacancyURL;
    private String title;
    private String broker;
    private String vacancyNumber;
    private String locationString;
    private Integer hours;
    private String salary;
    private LocalDateTime postingDate;
    private String about;
    private String company;

}
