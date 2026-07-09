package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import org.hibernate.annotations.Type;
import org.springframework.context.annotation.Profile;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Tracks the progress of an asynchronous spectrum deletion job. A row is created with status
 * SCHEDULED when a delete is enqueued, flipped to RUNNING by the listener, and finally COMPLETE
 * or FAILED. The row is the source of truth for progress polling and survives a broker restart
 */
@Entity
@Table(name = "deletion_job")
@Profile({"mona.persistence"})
public class DeletionJob implements Serializable {
    public static final String STATUS_SCHEDULED = "SCHEDULED";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_COMPLETE = "COMPLETE";
    public static final String STATUS_FAILED = "FAILED";

    @Id
    private String id;

    // RSQL/filter query for a query-based delete, null for an id-based delete
    @Type(type = "org.hibernate.type.TextType")
    private String query;

    // Comma joined mona ids for an id-based delete, null for a query-based delete
    @Type(type = "org.hibernate.type.TextType")
    private String spectrumIds;

    private String emailAddress;

    private Date date;

    private Date lastUpdated;

    private String status;

    // Total spectra matched at enqueue time, known up front so the bar starts at 0/total
    private Long total;

    private Long deleted;

    // Spectra that could not be deleted and were skipped and logged
    private Long skipped;

    @Type(type = "org.hibernate.type.TextType")
    private String errorMessage;

    // Set when this deletion was enqueued from an upload's delete button, so the listener can flip
    // that UploadJob to DELETED once this job completes. Null for a standalone/admin mass delete
    private String uploadJobId;

    public DeletionJob() {
        this.deleted = 0L;
        this.skipped = 0L;
    }

    public DeletionJob(String id, String query, String spectrumIds, String emailAddress, Date date, String status, Long total) {
        this.id = id;
        this.query = query;
        this.spectrumIds = spectrumIds;
        this.emailAddress = emailAddress;
        this.date = date;
        this.lastUpdated = date;
        this.status = status;
        this.total = total;
        this.deleted = 0L;
        this.skipped = 0L;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSpectrumIds() {
        return spectrumIds;
    }

    public void setSpectrumIds(String spectrumIds) {
        this.spectrumIds = spectrumIds;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getDeleted() {
        return deleted;
    }

    public void setDeleted(Long deleted) {
        this.deleted = deleted;
    }

    public Long getSkipped() {
        return skipped;
    }

    public void setSkipped(Long skipped) {
        this.skipped = skipped;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getUploadJobId() {
        return uploadJobId;
    }

    public void setUploadJobId(String uploadJobId) {
        this.uploadJobId = uploadJobId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeletionJob that = (DeletionJob) o;
        return Objects.equals(id, that.id) && Objects.equals(query, that.query) && Objects.equals(spectrumIds, that.spectrumIds) && Objects.equals(emailAddress, that.emailAddress) && Objects.equals(date, that.date) && Objects.equals(lastUpdated, that.lastUpdated) && Objects.equals(status, that.status) && Objects.equals(total, that.total) && Objects.equals(deleted, that.deleted) && Objects.equals(skipped, that.skipped) && Objects.equals(errorMessage, that.errorMessage) && Objects.equals(uploadJobId, that.uploadJobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, query, spectrumIds, emailAddress, date, lastUpdated, status, total, deleted, skipped, errorMessage, uploadJobId);
    }
}
