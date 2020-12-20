package nl.ordina.jobcrawler.scrapers;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.exception.HTMLStructureException;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.service.DocumentService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

@Slf4j
@Component
public class StaffingGroupScraper extends VacancyScraper {

    private DocumentService documentService = new DocumentService();
    private final Pattern ymdPattern = Pattern.compile("^[0-9]{4}-(1[0-2]|0[1-9])-(3[01]|[12][0-9]|0[1-9])$");

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public StaffingGroupScraper() {
        super(
                "https://www.destaffinggroep.nl/opdrachten/", // Required search URL. Can be retrieved using getSEARCH_URL()
                "StaffingGroup" // Required broker. Can be retrieved using getBROKER()
        );
    }

    @Override
    public List<VacancyDTO> getVacancies() {
        Map<String, LocalDate> vacancyURLsAndPubDates = retrieveURLsAndPubDates();
        return retrieveVacancies(vacancyURLsAndPubDates);
    }

    private Map<String, LocalDate> retrieveURLsAndPubDates() {
        log.info("{} -- Start scraping", getBroker().toUpperCase());
        Map<String, LocalDate> vacancyURLsAndPubDates = new HashMap<>() {};
        try {
            String URL = getSearchUrl();
            Document doc = documentService.getDocument(getSearchUrl());
            Elements elements = doc.select("a.assignment-card:not(.is-rendered)");
            Elements elements1 = doc.select("p.assignment-card__age-label");
            for (int i = 0; i<elements.size(); i++) {
                String sLink = elements.get(i).attr("abs:href");
                sLink = sLink.substring(0,sLink.indexOf("opdracht/")+9) + sLink.substring(sLink.length()-6,sLink.length());
                LocalDate date = null;
                String dateString = elements1.get(i).children().text();
                if(dateString.contains("Nieuw")) {
                    date = LocalDate.now();
                } else if(dateString.contains("dag")) {
                    date = LocalDate.now().minus(Period.ofDays(Integer.parseInt(dateString.substring(0,dateString.indexOf(' ')))));
                } else if(dateString.contains("we")) {
                    date = LocalDate.now().minus(Period.ofWeeks(Integer.parseInt(dateString.substring(0,dateString.indexOf(' ')))));
                } else if(dateString.contains("maand")) {
                    date = LocalDate.now().minus(Period.ofMonths(Integer.parseInt(dateString.substring(0,dateString.indexOf(' ')))));
                }
                vacancyURLsAndPubDates.put(sLink,date); //424 .  span data-new-text
            }
/*            Element element = elements.first();
            Element parent = element.parent();
            Elements lijst = parent.children();
            Elements httplinks = lijst.select("a[href]");
            for (Element e : httplinks) {
                String sLink = e.attr("abs:href");
                String vacancyLink = sLink.contains("?") ? sLink.split("\\?")[0] : sLink;
                vacancyUrlsOnPage.add(vacancyLink);
            }*/
        } catch (HTMLStructureException e) {
            log.error(e.getMessage());
        }   return vacancyURLsAndPubDates;
    }

    private List<VacancyDTO> retrieveVacancies(Map<String, LocalDate> vacancyURLsAndPubDates) {
        List<VacancyDTO> vacancies = new CopyOnWriteArrayList<>();
        vacancyURLsAndPubDates.keySet().forEach(u -> {
            System.out.println("retrieveVacancies-method: " + u);
            Document doc = documentService.getDocument(u);
            if (doc != null) {
                VacancyDTO vacancyDTO = VacancyDTO.builder()
                        .vacancyURL(u)
                        .title(getVacancyTitle(doc))
                        .hours(getWorkHours(doc))
                        .broker(getBroker())
                        .locationString(getLocation(doc))
                        .postingDate(LocalDateTime.of(vacancyURLsAndPubDates.get(u), LocalTime.of(00,00)))
                        .about(getVacancyAbout(doc))
                        .company(getCompanyName(doc))
                        .build();
                vacancies.add(vacancyDTO);
                log.info("{} - Vacancy found: {}", getBroker(), vacancyDTO.getTitle());
           }
        });
        log.info("{} -- Returning scraped vacancies", getBroker());
        return vacancies;
    }

    private String getVacancyTitle(Document doc) {
        Elements elements = doc.select("h2");
        elements.removeIf(e -> !e.text().contains("Titel"));
        if (!elements.isEmpty() && elements.first()!=null && elements.first().nextElementSibling()!=null) {
            return elements.first().nextElementSibling().text();
        } else {
            return "";
        }
    }
    private Integer getWorkHours(Document doc) {
        Elements elements = doc.select("h3");
        elements.removeIf(e -> !e.text().contains("Uren per week"));
        if (!elements.isEmpty() && elements.first()!=null && elements.first().nextElementSibling()!=null) {
            return Integer.parseInt(elements.first().nextElementSibling().text());
        } else {
            return 0;
        }
    }
    private String getLocation(Document doc) {
        Elements elements = doc.select("h3");
        elements.removeIf(e -> !e.text().contains("Locatie"));
        if (!elements.isEmpty() && elements.first()!=null && elements.first().nextElementSibling()!=null) {
            return elements.first().nextElementSibling().text();
        } else {
            return "";
        }
    }

    /**
     * Retrieve publishing date from vacancy
     *
     * @param doc Document which is needed to retrieve publishing date
     * @return String publish date
     */

    private boolean checkDatePattern(String s) {
        return s != null && ymdPattern.matcher(s).matches();
    }

    /**
     * Retrieve the vacancy body to store in postgres database
     *
     * @param doc Document which is needed to retrieve the body
     * @return String vacancy body
     */
    private String getVacancyAbout(Document doc) {
        String about = "Organisatie:\n";
        Elements elements = doc.select("h2");
        elements.removeIf(e -> !(e.tag().getName().equals("h2")&&e.text().contains("Organisatie")));
        System.out.println("getVacancyAbout-method: " + doc.location());
        if(elements.size()==0) {
            System.out.println("test");
        }
        Elements els1 = elements.get(0).nextElementSiblings();
        for (int i=0; i<els1.size(); i++) {
            Element el = els1.get(i);
            if(el.hasText() & !el.tag().getName().equals("h2")) {
                about = about + el.text() + "\n";
            }
            if(el.hasText() & el.tag().getName().equals("h2")) {
                about = about + "\n" + el.text() + ":\n";
            }
        }
        while(about.substring(0,2).equals("\n")) {
            about = about.substring(2);
        }
        while(about.substring(about.length()-2).equals("\n")) {
            about = about.substring(0,about.length()-2);
        }
        return about;
    }
/*


        for(int i = 0; i < aboutCats.size(); i++) {
            if (elements.get(0)!=null && elements.get(0).nextElementSibling()!=null && elements.get(0).nextElementSibling().child(0)!=null) {
                organisatie = elements.get(0).nextElementSibling().child(0).text();
            } else {
                organisatie = "";
            }
        }
*/

    /**
     * Retrieves company name
     *
     * @param doc Document which is needed to retrieve the company name
     * @return String company name
     */
    private String getCompanyName(Document doc) {
        return "";
    }
}
