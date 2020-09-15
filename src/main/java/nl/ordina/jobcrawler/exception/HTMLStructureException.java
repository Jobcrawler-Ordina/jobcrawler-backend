package nl.ordina.jobcrawler.exception;

public class HTMLStructureException extends RuntimeException {
    public HTMLStructureException(String s) {
        super("HTML structure altered in a critical way:" + s);
    }
}
