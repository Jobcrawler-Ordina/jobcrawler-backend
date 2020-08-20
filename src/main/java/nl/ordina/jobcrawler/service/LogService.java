package nl.ordina.jobcrawler.service;


// Added to assert that logging is called in certain situations
// for test purposes

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogService {

    public void logInfo(String aMessage) {
        log.info(aMessage);
    }

    public void logError(String aMessage) {
        log.error(aMessage);
    }
}
