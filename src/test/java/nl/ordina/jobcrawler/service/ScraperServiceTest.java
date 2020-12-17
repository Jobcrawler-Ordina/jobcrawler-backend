package nl.ordina.jobcrawler.service;

import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.scrapers.HuxleyITVacancyScraper;
import nl.ordina.jobcrawler.scrapers.JobBirdScraper;
import nl.ordina.jobcrawler.scrapers.YachtVacancyScraper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScraperServiceTest {
    private static final String URL = "url";
    private static final String LOCATION_STRING = "'s-Hertogenbosch, Nederland";

    @Mock
    VacancyService mockVacancyService;

    @Mock
    LocationService mockLocationService;

    @Mock
    YachtVacancyScraper mockYachtVacancyScraper;

    @Mock
    HuxleyITVacancyScraper mockHuxleyITVacancyScraper;

    @Mock
    JobBirdScraper mockJobBirdScraper;

    @Mock
    ModelMapper mockModelMapper;

    @InjectMocks
    ScraperService scraperService;

    Vacancy vacancy;
    VacancyDTO vacancyDTO;
    List<Vacancy> vacancyList;
    List<VacancyDTO> vacancyDTOList;

    @BeforeEach
    void setUp() {
        vacancy = new Vacancy();
        vacancyDTO = new VacancyDTO();
        vacancyDTO.setVacancyURL(URL);
        vacancyDTO.setLocationString(LOCATION_STRING);
        vacancyList = Collections.singletonList(vacancy);
        vacancyDTOList = Collections.singletonList(vacancyDTO);

        lenient().when(mockYachtVacancyScraper.getVacancies()).thenReturn(vacancyDTOList);
        lenient().when(mockHuxleyITVacancyScraper.getVacancies()).thenReturn(vacancyDTOList);
        lenient().when(mockJobBirdScraper.getVacancies()).thenReturn(vacancyDTOList);
        lenient().when(mockModelMapper.map(vacancyDTO, Vacancy.class)).thenReturn(vacancy);
        lenient().when(mockVacancyService.save(vacancy)).thenReturn(vacancy);
    }

    @Test
    void testScrapeOnlyExisting() {
        when(mockVacancyService.findByURL(URL)).thenReturn(Optional.of(vacancy));

        scraperService.scrape();

        verify(mockVacancyService, times(3)).findByURL(URL);
        verifyScrapers();
    }

    @Test
    void testScrapeNewVacancyExistingLocation() {
        when(mockVacancyService.findByURL(URL)).thenReturn(Optional.empty());
        when(mockLocationService.findByLocationName("Den Bosch")).thenReturn(Optional.of(new Location("Den Bosch")));

        scraperService.scrape();

        verify(mockVacancyService, times(3)).findByURL(URL);
        verifyScrapers();
        verify(mockModelMapper, times(3)).map(vacancyDTO, Vacancy.class);
        verify(mockLocationService, times(3)).findByLocationName("Den Bosch");
        verify(mockVacancyService, times(3)).save(vacancy);

        assertEquals("Den Bosch", vacancy.getLocation().getName());
    }

    @Test
    void testScrapeNewVacancyNewLocation() throws Exception {
        when(mockVacancyService.findByURL(URL)).thenReturn(Optional.empty());
        when(mockLocationService.findByLocationName("Den Bosch")).thenReturn(Optional.empty());
        when(mockLocationService.getCoordinates("Den Bosch")).thenReturn(new double[]{52.3727598, 4.8936041});
        when(mockLocationService.save(any(Location.class))).thenReturn(mock(Location.class));

        scraperService.scrape();

        verify(mockVacancyService, times(3)).findByURL(URL);
        verifyScrapers();
        verify(mockModelMapper, times(3)).map(vacancyDTO, Vacancy.class);
        verify(mockLocationService, times(3)).findByLocationName("Den Bosch");
        verify(mockLocationService, times(3)).getCoordinates("Den Bosch");
        verify(mockLocationService, times(3)).save(any(Location.class));
        verify(mockVacancyService, times(3)).save(vacancy);

        assertEquals("Den Bosch", vacancy.getLocation().getName());
    }

    @Test
    void testDeleteNoMoreExistingVacancies() {
        UUID uuid = UUID.randomUUID();
        vacancy.setId(uuid);
        when(mockVacancyService.findAll()).thenReturn(vacancyList);
        when(mockVacancyService.hasExistingURL(vacancy)).thenReturn(false);
        doNothing().when(mockVacancyService).delete(uuid);

        scraperService.deleteNoMoreExistingVacancies();

        verify(mockVacancyService, times(1)).findAll();
        verify(mockVacancyService, times(1)).hasExistingURL(vacancy);
        verify(mockVacancyService, times(1)).delete(uuid);
    }

    private void verifyScrapers() {
        verify(mockYachtVacancyScraper, times(1)).getVacancies();
        verify(mockHuxleyITVacancyScraper, times(1)).getVacancies();
        verify(mockJobBirdScraper, times(1)).getVacancies();
    }
}
