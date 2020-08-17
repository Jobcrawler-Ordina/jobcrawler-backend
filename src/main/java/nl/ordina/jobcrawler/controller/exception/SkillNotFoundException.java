package nl.ordina.jobcrawler.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SkillNotFoundException extends RuntimeException {
    public SkillNotFoundException(String skillName) {
        super("Skill not found for: " + skillName);
    }
    public SkillNotFoundException(UUID id) {
        super("Skill not found with id " + id);
    }
}
