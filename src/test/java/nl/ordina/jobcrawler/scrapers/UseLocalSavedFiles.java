package nl.ordina.jobcrawler.scrapers;

import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

public class UseLocalSavedFiles {

    // This method is used to retrieve the file content for local saved html files.
    protected static File getFile(String fileName) {
        try {
            return new ClassPathResource(fileName).getFile();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
