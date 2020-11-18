package nl.ordina.jobcrawler.repo;

import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Location_;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.model.Vacancy_;
import nl.ordina.jobcrawler.payload.SearchRequest;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Vacancy specification to query the database with JpaSpecificationExecutor.
 */
@Component
public class VacancySpecifications {

    private static final String LIKE_QUERY_FORMAT = "%%%s%%";

    private final EntityManager entityManager;
    private final ModelMapper modelMapper;

    public VacancySpecifications(EntityManager entityManager, ModelMapper modelMapper) {
        this.entityManager = entityManager;
        this.modelMapper = modelMapper;
    }

    public Long totalMatchingVacancies(final SearchRequest searchRequest) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Vacancy> rootCount = countQuery.from(Vacancy.class);
        Join<Vacancy, Location> locationJoinRoot = rootCount.join(Vacancy_.location, JoinType.LEFT);

        List<Predicate> allPredicates = getPredicates(searchRequest, rootCount, locationJoinRoot, criteriaBuilder);

        countQuery.select(criteriaBuilder.count(rootCount));
        countQuery.where(criteriaBuilder.and(allPredicates.toArray(new Predicate[0])));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    public List<VacancyDTO> getMatchingVacancies(final SearchRequest searchRequest, Pageable paging, String[] sort) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<Vacancy> root = query.from(Vacancy.class);
        Join<Vacancy, Location> locationJoin = root.join(Vacancy_.location, JoinType.LEFT);

        if (searchRequest.getDistance() != null) {
            query.multiselect(criteriaBuilder.function(
                    "getDistance", Double.class, criteriaBuilder.literal(searchRequest.getCoord()[0]), criteriaBuilder.literal(searchRequest.getCoord()[1]),
                    locationJoin.get(Location_.lat), locationJoin.get(Location_.lon)
            ).alias("dist"), root);
        } else {
            query.multiselect(root);
        }

        List<VacancyDTO> vacancyDTOList = new ArrayList<>();
        List<Predicate> predicateList = getPredicates(searchRequest, root, locationJoin, criteriaBuilder);

        query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])));

        Expression expression;
        switch (sort[0]) {
            case "distance":
                expression = criteriaBuilder.literal(1);
                break;
            case "location.name":
                expression = locationJoin.get("name");
                break;
            default:
                expression = root.get(sort[0]);
                break;
        }

        Order order = sort[1].equals("asc") ? criteriaBuilder.asc(expression) : criteriaBuilder.desc(expression);
        query.orderBy(order);

        try {
            List<Tuple> list = entityManager.createQuery(query)
                    .setFirstResult(paging.getPageNumber() * paging.getPageSize())
                    .setMaxResults(paging.getPageSize())
                    .getResultList();
            for (Tuple t : list) {
                VacancyDTO vacancy = modelMapper.map(t.get(root), VacancyDTO.class);
                if (searchRequest.getDistance() != null) {
                    vacancy.setDistance((Double) t.get("dist"));
                }
                vacancyDTOList.add(vacancy);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vacancyDTOList;
    }

    private List<Predicate> getPredicates(SearchRequest searchRequest, Root<Vacancy> root, Join<Vacancy, Location> locationJoin, CriteriaBuilder cb) {
        List<Predicate> allPredicates = new ArrayList<>();
        Optional<SearchRequest> optionalProperties = Optional.of(searchRequest);

        optionalProperties.map(SearchRequest::getLocation)
                .filter(l -> !l.isBlank() && optionalProperties.map(SearchRequest::getDistance).isEmpty())
                .ifPresent(location -> allPredicates
                        .add(cb.like(cb.lower(locationJoin.get(Location_.name)), String
                                .format(LIKE_QUERY_FORMAT, location.toLowerCase()))));

        optionalProperties.map(SearchRequest::getDistance).filter(dist -> (dist != 0))
                .ifPresent(dist -> optionalProperties.map(SearchRequest::getCoord).ifPresent(coord -> allPredicates
                        .add(cb.le(cb
                                .function("getDistance", Double.class, cb.literal(searchRequest.getCoord()[0]), cb
                                        .literal(searchRequest.getCoord()[1]), locationJoin
                                        .get(Location_.lat), locationJoin
                                        .get(Location_.lon)), dist))));

        optionalProperties.map(SearchRequest::getKeywords).filter(t -> !t.isEmpty())
                .ifPresent(keywords -> allPredicates.add(cb.or(cb.like(cb.lower(root.get(Vacancy_.about)), String
                        .format(LIKE_QUERY_FORMAT, keywords.toLowerCase())), cb
                        .like(cb.lower(root.get(Vacancy_.title)), String
                                .format(LIKE_QUERY_FORMAT, keywords.toLowerCase())), cb
                        .like(cb.lower(root.get(Vacancy_.company)), String
                                .format(LIKE_QUERY_FORMAT, keywords.toLowerCase())))));

        optionalProperties.map(SearchRequest::getSkills).filter(t -> !t.isEmpty()).ifPresent(skills -> allPredicates
                .add(cb.and(skills.stream()
                        .map(s -> cb.like(root.get(Vacancy_.about), String.format(LIKE_QUERY_FORMAT, s)))
                        .toArray(Predicate[]::new))));

        optionalProperties.map(SearchRequest::getFromDate).ifPresent(fromDate -> allPredicates
                .add(cb.greaterThanOrEqualTo(root.get(Vacancy_.postingDate), cb.literal(fromDate))));

        optionalProperties.map(SearchRequest::getToDate).ifPresent(toDate -> allPredicates
                .add(cb.lessThanOrEqualTo(root.get(Vacancy_.postingDate), cb.literal(toDate))));

        return allPredicates;
    }

}
