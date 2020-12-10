package nl.ordina.jobcrawler.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SkillDTO {
    private UUID id;
    private String name;
}
