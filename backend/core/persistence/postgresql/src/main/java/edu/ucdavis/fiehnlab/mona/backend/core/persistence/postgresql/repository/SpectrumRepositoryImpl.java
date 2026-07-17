package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import com.turkraft.springfilter.boot.FilterSpecification;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Keyset (cursor) based export paging implementation. Returns a plain list, so it avoids the
 * per-page count query and the growing offset that Page based paging incurs on large exports
 */
public class SpectrumRepositoryImpl implements SpectrumRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    // Resolves spectrum ids matching the keyword per branch so each branch can use its own
    // trigram (or FK) index. An OR spanning these joined tables instead runs as a post join
    // filter over the whole join product and can never use an index
    private static final String KEYWORD_MATCH_SQL =
            "SELECT m.spectrum_metadata_id AS id FROM metadata m " +
            "WHERE m.value ILIKE :pattern AND m.spectrum_metadata_id IS NOT NULL " +
            "UNION " +
            "SELECT c.spectrum_id AS id FROM name n JOIN compound c ON c.id = n.compound_id " +
            "WHERE n.name ILIKE :pattern " +
            "UNION " +
            "SELECT c.spectrum_id AS id FROM metadata m JOIN compound c ON c.id = m.compound_metadata_id " +
            "WHERE m.value ILIKE :pattern";

    // Escapes LIKE metacharacters so the user's term is always matched literally
    private static String likePattern(String keyword) {
        return "%" + keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Spectrum> findByKeyword(String keyword, int page, int size) {
        return entityManager.createNativeQuery(
                        "SELECT s.* FROM spectrum s WHERE s.id IN (" + KEYWORD_MATCH_SQL + ") ORDER BY s.id DESC",
                        Spectrum.class)
                .setParameter("pattern", likePattern(keyword))
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public long countByKeyword(String keyword) {
        Object result = entityManager.createNativeQuery(
                        "SELECT count(*) FROM (" + KEYWORD_MATCH_SQL + ") matches")
                .setParameter("pattern", likePattern(keyword))
                .getSingleResult();
        return ((Number) result).longValue();
    }

    @Override
    public List<Spectrum> findForExport(String query, String lastId, int limit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Spectrum> cq = cb.createQuery(Spectrum.class);
        Root<Spectrum> root = cq.from(Spectrum.class);

        List<Predicate> predicates = new ArrayList<>();

        // Reuse the exact filter the Page based export used so the returned rows are identical.
        // FilterSpecification (not the distinct variant) matches the existing export query
        if (query != null && !query.isEmpty()) {
            Specification<Spectrum> spec = new FilterSpecification<>(query);
            Predicate filterPredicate = spec.toPredicate(root, cq, cb);

            if (filterPredicate != null) {
                predicates.add(filterPredicate);
            }
        }

        // Keyset cursor: walk down the id ordering instead of paging by offset
        Path<String> id = root.get("id");

        if (lastId != null) {
            predicates.add(cb.lessThan(id, lastId));
        }

        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        // Same ordering the export already used so output is byte equivalent
        cq.orderBy(cb.desc(id));

        return entityManager.createQuery(cq)
                .setMaxResults(limit)
                .getResultList();
    }
}
