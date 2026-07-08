package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * AMQP message enqueued when an uploaded file is ready to be parsed and persisted. It carries
 * only the job id, the matching UploadJob row holds the file location and progress so the job is
 * fully recoverable from the database even if the message is lost
 */
public class UploadJobRequest implements Serializable {
    private String jobId;

    public UploadJobRequest() {}

    public UploadJobRequest(String jobId) {
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
        UploadJobRequest that = (UploadJobRequest) o;
        return Objects.equals(jobId, that.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }
}
