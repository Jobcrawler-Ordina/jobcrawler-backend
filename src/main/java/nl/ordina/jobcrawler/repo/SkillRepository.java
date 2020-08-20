package nl.ordina.jobcrawler.repo;

import nl.ordina.jobcrawler.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {


    @Query(value = "delete from vacancy_skills where skill_id in (select id from skill where name = ?1)",
            nativeQuery = true)
    @Transactional
    @Modifying
    void deleteReferencesToSkill(String name);

    @Transactional
    void deleteSkillByName(String name);

    @Query(value = "delete from vacancy_skills",
            nativeQuery = true)
    @Transactional
    @Modifying
    void deleteReferencesToSkills();

    List<Skill> findByOrderByNameAsc();

    @Query(value = "insert into vacancy_skills(vacancy_id, skill_id) values (?1, ?2)",
            nativeQuery = true)
    @Transactional
    @Modifying
    void linkSkillToVacancy(UUID vacancyId, UUID skillID);
}
