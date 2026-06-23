package edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics;

/**
 * Lightweight projection holding the aggregated statistics for a single submitter, grouped
 * by email address in the database so the rebuild does not have to stream every submitter row
 */
public class SubmitterAggregation {
    private final String emailAddress;
    private final String firstName;
    private final String lastName;
    private final String institution;
    private final Long count;
    private final Double score;

    public SubmitterAggregation(String emailAddress, String firstName, String lastName, String institution, Long count, Double score) {
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.lastName = lastName;
        this.institution = institution;
        this.count = count;
        this.score = score;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getInstitution() {
        return institution;
    }

    public Long getCount() {
        return count;
    }

    public Double getScore() {
        return score;
    }
}
