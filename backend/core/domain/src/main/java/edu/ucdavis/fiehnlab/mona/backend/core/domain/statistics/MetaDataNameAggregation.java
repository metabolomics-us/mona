package edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics;

/**
 * Lightweight projection holding a single metadata name and the number of metadata rows
 * that share it. Populated by a database side GROUP BY aggregation so the statistics rebuild
 * can record the per name totals without streaming and counting every metadata row in the JVM
 */
public class MetaDataNameAggregation {
    private final String name;
    private final Long count;

    public MetaDataNameAggregation(String name, Long count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public Long getCount() {
        return count;
    }
}
