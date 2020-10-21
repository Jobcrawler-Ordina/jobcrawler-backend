package nl.ordina.jobcrawler.repo;

import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.payload.SearchRequest;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Vacancy specification to query the database with JpaSpecificationExecutor.
 */
public final class VacancySpecifications {

    private static final String LIKE_QUERY_FORMAT = "%%%s%%";

    private VacancySpecifications() {
    }

    /**
     * This query will filter the vacancies by any search value that is entered in the search form.
     *
     * @param searchRequest - SearchRequest values
     * @return - filtered vacancies
     */
    public static Specification<Vacancy> vacancySearch(final SearchRequest searchRequest) {
        return (root, query, cb) -> {
            List<Predicate> allPredicates = new ArrayList<>();
            Optional<SearchRequest> optionalProperties = Optional.of(searchRequest);

            optionalProperties.map(SearchRequest::getLocation).filter(l -> !l.isBlank())
                    .ifPresent(location -> allPredicates.add(cb.like(cb.lower(root.get("location").get("name")), String
                            .format(LIKE_QUERY_FORMAT, location.toLowerCase()))));

            optionalProperties.map(SearchRequest::getDistance).filter(dist -> !(dist == 0))
                    .ifPresent(dist -> optionalProperties.map(SearchRequest::getCoord).ifPresent(coord -> allPredicates
                            .add(cb.le(cb
                                    .function("getDistance", Double.class, cb.literal(searchRequest.getCoord()[0]), cb
                                            .literal(searchRequest.getCoord()[1]), root.get("location").get("lon"), root
                                            .get("location").get("lat")), dist))));

            optionalProperties.map(SearchRequest::getKeywords).filter(t -> !t.isEmpty())
                    .ifPresent(keywords -> allPredicates.add(cb.or(cb.like(cb.lower(root.get("about")), String
                            .format(LIKE_QUERY_FORMAT, keywords.toLowerCase())), cb
                            .like(cb.lower(root.get("title")), String
                                    .format(LIKE_QUERY_FORMAT, keywords.toLowerCase())), cb
                            .like(cb.lower(root.get("company")), String
                                    .format(LIKE_QUERY_FORMAT, keywords.toLowerCase())))));

            optionalProperties.map(SearchRequest::getSkills).filter(t -> !t.isEmpty()).ifPresent(skills -> allPredicates
                    .add(cb.and(skills.stream()
                            .map(s -> cb.like(root.get("about"), String.format(LIKE_QUERY_FORMAT, s)))
                            .toArray(Predicate[]::new))));

            optionalProperties.map(SearchRequest::getFromDate).ifPresent(fromDate -> allPredicates
                    .add(cb.greaterThanOrEqualTo(root.get("postingDate"), cb.literal(fromDate))));

            optionalProperties.map(SearchRequest::getToDate).ifPresent(toDate -> allPredicates
                    .add(cb.lessThanOrEqualTo(root.get("postingDate"), cb.literal(toDate))));


            return cb.and(allPredicates.toArray(new Predicate[0]));
        };
    }

}
