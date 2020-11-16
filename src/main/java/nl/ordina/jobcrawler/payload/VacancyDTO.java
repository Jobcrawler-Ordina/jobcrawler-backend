package nl.ordina.jobcrawler.payload;

import lombok.*;
import nl.ordina.jobcrawler.model.Location;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VacancyDTO {

    private UUID id;
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
    private Location location;
    private double distance;

}
