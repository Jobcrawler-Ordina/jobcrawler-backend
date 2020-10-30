package nl.ordina.jobcrawler.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import nl.ordina.jobcrawler.exception.VacancyURLMalformedException;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Vacancy {
    /* this class will be saved in a table called vacancy
     *   a linking table called vacancy_skills will contain vacancy ID's and their corresponding skill ID's
     * */

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "uuid-char")
    private UUID id;
    @NotNull
    private String vacancyURL;
    private String title;
    private String broker;
    private String vacancyNumber;
    private Integer hours;
    private String salary;
    @JsonFormat(timezone = "Europe/Amsterdam", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime postingDate;
    @Column(columnDefinition = "TEXT")
    private String about;
    private String company;

    @ManyToMany
    @JoinTable(
            name = "vacancy_skills",
            joinColumns = @JoinColumn(name = "vacancy_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    @JsonIgnore
    Set<Skill> skills;  //a set is a collection that has no duplicates

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    Location location;

    public boolean hasLocation() {
        return this.location != null;
    }

    public boolean hasValidURL() {
        if (!this.vacancyURL.startsWith("http"))
            this.vacancyURL = "https://" + this.vacancyURL; //adding the protocol, if not present

        URL url;
        HttpURLConnection huc;
        int responseCode;

        try {
            url = new URL(this.vacancyURL);
            huc = (HttpURLConnection) url.openConnection();
            huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            huc.setRequestMethod("HEAD");   // faster because it doesn't download the response body
            /*
             * Added a user agent as huxley gives a 403 forbidden error
             * This user agent will make it as if we are making the request from a modern browser
             */

            if (this.broker.equals("Jobbird")) {
                if (huc.getResponseCode() != 200) {
                    return false;
                }

                String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36 ArabotScraper";
                Document doc = Jsoup.connect(this.vacancyURL).userAgent(userAgent).get();
                Elements alertsDanger = doc.select(".alert-danger");
                for (Element alert : alertsDanger) {
                    if (alert.text().contains("niet langer actief")) {
                        return false;
                    }
                }
                return true;
            } else {
                responseCode = huc.getResponseCode();
                return responseCode == 200; //returns true if the website has a 200 OK response
            }
        } catch (IOException e) {
            throw new VacancyURLMalformedException(this.vacancyURL);
        }
    }

    @Override
    public String toString() {
        String message;
        message = "Vacancy{" +
                "id=" + id +
                ", vacancyURL='" + vacancyURL + '\'' +
                ", title='" + title + '\'' +
                ", broker='" + broker + '\'' +
                ", vacancyNumber='" + vacancyNumber + '\'' +
                ", hours='" + hours + '\'' +
                ", salary='" + salary + '\'' +
                ", postingDate='" + postingDate + '\'' +
                ", about='" + about + '\'' +
                ", company='" + company + '\'' +
                ", skills=" + skills +
                ", location_filled=" + !(location==null);
            if(!(location==null)) {
                message = message + ", location_name=" + location.getName();
            }
            message = message + '}';
            return message;
    }
}
