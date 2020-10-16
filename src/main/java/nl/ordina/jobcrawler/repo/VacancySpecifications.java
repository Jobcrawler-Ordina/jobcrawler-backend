package nl.ordina.jobcrawler.repo;

import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.service.LocationService;
import org.json.JSONException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class VacancySpecifications {

    private static final String LIKE_QUERY_FORMAT = "%%%s%%";

    private VacancySpecifications() {
    }

    /**
     * Vacancy specification to query the database with JpaSpecificationExecutor.
     *
     * @param skills List of skill to filter the about attribute
     * @return predicate which can contain more predicates.
     */
    public static Specification<Vacancy> findBySkill(final Set<String> skills) {
        return (root, query, cb) ->
          cb.and(skills.stream().map(s -> cb
                    .like(root.get("about"), String
                            .format(LIKE_QUERY_FORMAT, s))).toArray(Predicate[]::new));
    }

    /**
     * This query will filter the vacancies by any search value that is entered in the search field.
     * @param value - search value
     * @return - filtered vacancies
     */
    public static Specification<Vacancy> findByValue(final String value) {
        return (root, query, cb) -> {
            List<Predicate> allPredicates = new ArrayList<>();
            allPredicates.add(cb.like(cb.lower(root.get("about")), String.format(LIKE_QUERY_FORMAT, value.toLowerCase())));
            allPredicates.add(cb.like(cb.lower(root.get("title")), String.format(LIKE_QUERY_FORMAT, value.toLowerCase())));
            allPredicates.add(cb.like(cb.lower(root.get("company")), String.format(LIKE_QUERY_FORMAT, value.toLowerCase())));

            return cb.or(allPredicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Vacancy> findByLocName(final String loc) {
        return (root, query, cb) -> {
            List<Predicate> allPredicates = new ArrayList<>();
            allPredicates.add(cb.like(cb.lower(root.get("location").get("name")), String.format(LIKE_QUERY_FORMAT, loc.toLowerCase())));

            return cb.or(allPredicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Vacancy> findByDistance(final double[] coord, final double dist) {
        return (root, query, cb) -> {
            List<Predicate> allPredicates = new ArrayList<>();
            allPredicates.add(cb.le(cb.function("getDistance", Double.class, coord[0], cb.parameter(Double.class), coord[1], cb.parameter(Double.class), root.get("location").get("lon"), cb.parameter(Double.class), root.get("location").get("lat"), cb.parameter(Double.class)), dist) );
            return cb.or(allPredicates.toArray(new Predicate[0]));
        };
    }

}

