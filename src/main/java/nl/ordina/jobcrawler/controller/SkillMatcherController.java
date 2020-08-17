package nl.ordina.jobcrawler.controller;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.service.SkillMatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(path = "/skillmatcher")
public class SkillMatcherController {

    private final SkillMatcherService skillMatcherService;

    @Autowired
    public SkillMatcherController(SkillMatcherService skillMatcherService) {
        this.skillMatcherService = skillMatcherService;
    }

    // rematch the skills
    @PutMapping
    public void relinkSkills() {
        log.info("relink skills");
        Thread newThread = new Thread(skillMatcherService::relinkSkills);
        newThread.start();
    }
}
