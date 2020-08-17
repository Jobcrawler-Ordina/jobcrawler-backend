package nl.ordina.jobcrawler.controller;

import nl.ordina.jobcrawler.service.ScraperService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScraperController.class)
class ScraperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScraperService scraperService;

    /**
     * Tests the /scrape end point, if it calls the VacancyStarter scrape method
     */
    @Test
    void scrape() throws Exception {
        doNothing().when(scraperService).scrape();

        mockMvc.perform(put("/scrape"))
                .andExpect(status().isOk());

        verify(scraperService, times(1)).scrape();
    }
}