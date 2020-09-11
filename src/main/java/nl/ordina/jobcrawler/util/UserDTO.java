package nl.ordina.jobcrawler.util;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserDTO {
    private long id;

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    private Set<String> roles = new HashSet<>();

    public void addRole(String roleName) {
        roles.add(roleName);
    }
}
