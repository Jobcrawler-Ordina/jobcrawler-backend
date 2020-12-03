package nl.ordina.jobcrawler.service;

import nl.ordina.jobcrawler.exception.LocationNotFoundException;
import nl.ordina.jobcrawler.exception.VacancyURLMalformedException;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.payload.SearchRequest;
import nl.ordina.jobcrawler.repo.VacancyRepository;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

import static nl.ordina.jobcrawler.utils.MockData.mockVacancy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyServiceTest {

    @Mock
    VacancyRepository mockVacancyRepository;

    @Mock
    SkillService mockSkillService;

    @Mock
    LocationService mockLocationService;

    @InjectMocks
    VacancyService vacancyService;

    @Test
    void testFindById() {
        Vacancy vacancy = mockVacancy("title");
        UUID uuid = vacancy.getId();
        // Multiple thenReturns for different testcases
        when(mockVacancyRepository.findById(uuid))
                .thenReturn(Optional.of(vacancy))
                .thenReturn(Optional.empty());

        // 1. test with returned Vacancy
        Optional<Vacancy> returnedVacancy = vacancyService.findById(uuid);

        assertTrue(returnedVacancy.isPresent());

        // 2. test with no returned Vacancy
        returnedVacancy = vacancyService.findById(uuid);

        assertFalse(returnedVacancy.isPresent());

        verify(mockVacancyRepository, times(2)).findById(uuid);
    }

    @Test
    void testFindSkillsByVacancyId() {
        Vacancy vacancy = mockVacancy("title");
        UUID uuid = vacancy.getId();
        List<Skill> skills = Arrays.asList(createSkill("Java"), createSkill("Python"));
        when(mockSkillService.findAll()).thenReturn(skills);
        // Multiple thenReturns for different testcases
        when(mockVacancyRepository.findById(uuid))
                .thenReturn(Optional.of(vacancy))
                .thenReturn(Optional.empty());

        // 1. test with returned Skill
        Set<Skill> foundSkills = vacancyService.findSkillsByVacancyId(uuid);

        assertEquals(1, foundSkills.size());

        // 2. test with no returned Skill
        foundSkills = vacancyService.findSkillsByVacancyId(uuid);

        assertTrue(foundSkills.isEmpty());

        verify(mockVacancyRepository, times(2)).findById(uuid);
    }

    @Test
    void testFindAll() {
        List<Vacancy> vacancies = Arrays.asList(mockVacancy("vacancy1"), mockVacancy("vacancy2"));
        // Multiple thenReturns for different testcases
        when(mockVacancyRepository.findAll())
                .thenReturn(vacancies)
                .thenReturn(Collections.emptyList());

        // 1. test with list of Vacancy result
        List<Vacancy> foundVacancies = vacancyService.findAll();

        assertEquals(2, foundVacancies.size());

        // 2. test with empty result
        foundVacancies = vacancyService.findAll();

        assertTrue(foundVacancies.isEmpty());

        verify(mockVacancyRepository, times(2)).findAll();
    }

    @Test
    void testFindByAnyValue() throws Exception {
        final Vacancy vacancy = mockVacancy("title");
        final Page<Vacancy> mockVacancyPage = new PageImpl<>(Collections.singletonList(vacancy));
        Pageable paging = PageRequest.of(1, 15, Sort.Direction.ASC, "postingDate");
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setLocation("Amsterdam");
        double[] coordinates = new double[]{52.3727598, 4.8936041};

        when(mockVacancyRepository.findAll(ArgumentMatchers.<Specification<Vacancy>>any(), any(PageRequest.class))).thenReturn(mockVacancyPage);
        // Multiple thenReturns for each of the testcases
        when(mockLocationService.getCoordinates(searchRequest.getLocation()))
                .thenReturn(coordinates)
                .thenThrow(IOException.class)
                .thenThrow(LocationNotFoundException.class);

        // 1. test without location set in search request (LocationService mock not used here)
        Page<Vacancy> result = vacancyService.findByAnyValue(new SearchRequest(), paging);

        assertSame(vacancy, result.getContent().get(0));

        // 2. test with location set in search request
        result = vacancyService.findByAnyValue(searchRequest, paging);

        assertSame(vacancy, result.getContent().get(0));

        // 3. test with IOException thrown

        result = vacancyService.findByAnyValue(searchRequest, paging);

        assertSame(vacancy, result.getContent().get(0));

        // 4. test with LocationNotFoundException thrown

        result = vacancyService.findByAnyValue(searchRequest, paging);

        assertSame(vacancy, result.getContent().get(0));

        // verify mock calls
        verify(mockLocationService, times(3)).getCoordinates(anyString());
        verify(mockVacancyRepository, times(4)).findAll(ArgumentMatchers.<Specification<Vacancy>>any(), any(PageRequest.class));
    }

    @Test
    void testSave() {
        final Vacancy vacancy = mockVacancy("title");
        VacancyService spyVacancyService = spy(vacancyService);
        doReturn(true).when(spyVacancyService).hasExistingURL(vacancy);
        when(mockVacancyRepository.save(vacancy)).thenReturn(vacancy);

        Vacancy savedVacancy = spyVacancyService.save(vacancy);

        assertEquals(vacancy, savedVacancy);
        verify(mockVacancyRepository, times(1)).save(vacancy);
    }

    @Test
    void testSaveNonExistingVacancyURL() {
        final Vacancy vacancy = mockVacancy("title");
        VacancyService spyVacancyService = spy(vacancyService);
        doReturn(false).when(spyVacancyService).hasExistingURL(vacancy);

        assertThrows(VacancyURLMalformedException.class, () -> spyVacancyService.save(vacancy));
    }

    @Test
    void testSaveAll() {
        List<Vacancy> vacancies = Arrays.asList(mockVacancy("vacancy1"), mockVacancy("vacancy2"));
        when(mockVacancyRepository.saveAll(vacancies)).thenReturn(vacancies);

        vacancyService.saveAll(vacancies);

        verify(mockVacancyRepository, times(1)).saveAll(vacancies);
    }

    @Test
    void testDelete() {
        UUID uuid = UUID.randomUUID();
        doNothing().when(mockVacancyRepository).deleteById(uuid);

        vacancyService.delete(uuid);

        verify(mockVacancyRepository, times(1)).deleteById(uuid);
    }

    @Test
    void testFindByURL() {
        Vacancy vacancy = mockVacancy("vacancy1");
        String url = "URL";
        // Multiple thenReturns for different testcases
        when(mockVacancyRepository.findByVacancyURLEquals(url))
                .thenReturn(Optional.of(vacancy))
                .thenReturn(Optional.empty());

        // 1. test with Vacancy result
        Optional<Vacancy> foundVacancy = vacancyService.findByURL(url);

        assertTrue(foundVacancy.isPresent());

        // 2. test with empty result
        foundVacancy = vacancyService.findByURL(url);

        assertFalse(foundVacancy.isPresent());

        verify(mockVacancyRepository, times(2)).findByVacancyURLEquals(url);
    }

    @Test
    void testHasExistingURL() throws Exception {
        Vacancy vacancy = mockVacancy("title");
        VacancyService spyVacancyService = spy(vacancyService);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        doReturn(connection).when(spyVacancyService).openConnection(any());
        when(connection.getResponseCode()).thenReturn(200);

        boolean URLExists = spyVacancyService.hasExistingURL(vacancy);

        assertTrue(URLExists);

    }

    @Test
    void testHasJobbirdExistingURL() throws Exception {
        Vacancy vacancy = mockVacancy("title");
        vacancy.setBroker("Jobbird");
        VacancyService spyVacancyService = spy(vacancyService);
        Document document = mock(Document.class);
        Element element = mock(Element.class);
        Elements elements = new Elements(element);
        doReturn(document).when(spyVacancyService).getDocument(eq(vacancy), anyString());
        when(document.select(".alert-danger")).thenReturn(elements);
        when(element.text()).thenReturn("actief").thenReturn("niet langer actief");

        assertTrue(spyVacancyService.hasExistingURL(vacancy));

        assertFalse(spyVacancyService.hasExistingURL(vacancy));
    }

    private Skill createSkill(String name) {
        return new Skill(UUID.randomUUID(), name);
    }
}
