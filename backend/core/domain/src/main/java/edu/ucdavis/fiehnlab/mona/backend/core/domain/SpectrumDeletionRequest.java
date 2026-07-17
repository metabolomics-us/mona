package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * AMQP message enqueued when a spectrum deletion is requested. It carries only the job id, the
 * matching DeletionJob row holds the actual work payload (query or ids) and progress so the job
 * is fully recoverable from the database even if the message is lost
 */
public class SpectrumDeletionRequest implements Serializable {
    private String jobId;

    public SpectrumDeletionRequest() {}

    public SpectrumDeletionRequest(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpectrumDeletionRequest that = (SpectrumDeletionRequest) o;
        return Objects.equals(jobId, that.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }
}
