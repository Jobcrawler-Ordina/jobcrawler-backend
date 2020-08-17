package nl.ordina.jobcrawler.service;

import java.util.List;
import java.util.Optional;

public interface CRUDService<T, U> {
    /**
     * Returns the object with the specified id.
     *
     * @param id        ID of the object to retrieve.
     * @return          The requested object if found.
     */
    Optional<T> findById(U id);

    /**
     * Returns all objects in the database.
     *
     * @return          All objects in the database.
     */
    List<T> findAll();

    /**
     * Updates the specified object, identified by its id.
     *
     * @param t         The object to update.
     * @return          True if the update succeeded, otherwise false.
     */
    T update(U u, T t);

    /**
     * Saves the specified object to the database.
     *
     * @param t   The object to save to the database.
     * @return          The saved object.
     */
    T save(T t);

    /**
     * Deletes the object with the specified id.
     * @param id        The id of the object to delete.
     * @return          True if the operation was successful.
     */
    boolean delete(U id);
}
