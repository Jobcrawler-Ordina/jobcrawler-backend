package nl.ordina.jobcrawler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

    @GeneratedValue
    @Id
    @JsonIgnore
    @Type(type = "uuid-char")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "skills", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JsonIgnore
    Set<Vacancy> vacancies = new HashSet<>();

    @Override
    public String toString() {
        return this.name;
    }

}
