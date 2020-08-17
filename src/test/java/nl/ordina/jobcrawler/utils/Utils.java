package nl.ordina.jobcrawler.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String asJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
