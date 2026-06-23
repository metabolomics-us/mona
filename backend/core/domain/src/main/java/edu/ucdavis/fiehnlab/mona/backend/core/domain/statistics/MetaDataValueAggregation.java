package edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics;

/**
 * Lightweight projection holding a single metadata name/value pair and the number of
 * metadata rows that share it. Populated by a database side GROUP BY aggregation so the
 * statistics rebuild does not have to stream and count every metadata row in the JVM
 */
public class MetaDataValueAggregation {
    private final String name;
    private final String value;
    private final Long count;

    public MetaDataValueAggregation(String name, String value, Long count) {
        this.name = name;
        this.value = value;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Long getCount() {
        return count;
    }
}
