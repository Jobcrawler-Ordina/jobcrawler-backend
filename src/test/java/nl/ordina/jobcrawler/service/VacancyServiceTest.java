package nl.ordina.jobcrawler.service;

import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.payload.SearchRequest;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.repo.VacancyRepository;
import nl.ordina.jobcrawler.utils.MockData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class VacancyServiceTest {

    @InjectMocks
    VacancyService vacancyService;

    @Mock
    VacancyRepository mockVacancyRepository;

    @Test
    public void findByAnyValue() {
        final Vacancy vacancy = MockData.mockVacancy("title");
        final Page<Vacancy> mockVacancyPage = new PageImpl<>(Collections.singletonList(vacancy));
        SearchRequest searchRequest = new SearchRequest();
        Pageable paging = PageRequest.of(1, 15, Sort.Direction.ASC, "postingDate");

        when(mockVacancyRepository.findAll(ArgumentMatchers.<Specification<Vacancy>>any(), any(PageRequest.class))).thenReturn(mockVacancyPage);
//        final Page<VacancyDTO> result = vacancyService.findByAnyValue(searchRequest, paging);
//        assertSame(mockVacancyPage.getContent().get(0), result.getContent().get(0));
    }

}
