package nl.ordina.jobcrawler.model;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class SkillDTO {
    private String id;
    private String name;

    public SkillDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }


}
