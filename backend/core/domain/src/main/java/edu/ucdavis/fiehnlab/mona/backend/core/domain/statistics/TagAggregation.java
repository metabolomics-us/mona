package edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics;

/**
 * Lightweight projection holding the row count of a single tag text/ruleBased pair, grouped
 * in the database so the rebuild does not have to stream every tag row in the JVM
 */
public class TagAggregation {
    private final String text;
    private final Boolean ruleBased;
    private final Long count;

    public TagAggregation(String text, Boolean ruleBased, Long count) {
        this.text = text;
        this.ruleBased = ruleBased;
        this.count = count;
    }

    public String getText() {
        return text;
    }

    public Boolean getRuleBased() {
        return ruleBased;
    }

    public Long getCount() {
        return count;
    }
}
