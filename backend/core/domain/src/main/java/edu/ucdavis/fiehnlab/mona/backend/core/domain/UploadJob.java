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
 * Tracks the lifecycle of an asynchronous spectra upload. A row is created with status UPLOADING
 * while the raw file streams to disk in chunks, flipped to INTERRUPTED if a sweep finds no chunk
 * activity for too long, back to UPLOADING if the client resumes, then SCHEDULED once the whole
 * file has arrived and is enqueued, then RUNNING while the worker parses and persists, and
 * finally COMPLETE or FAILED. The row is the source of truth for progress polling and upload
 * history, so a closed tab or a service restart never loses an in flight upload
 */
@Entity
@Table(name = "upload_job")
@Profile({"mona.persistence"})
public class UploadJob implements Serializable {
    public static final String STATUS_UPLOADING = "UPLOADING";
    public static final String STATUS_INTERRUPTED = "INTERRUPTED";
    public static final String STATUS_SCHEDULED = "SCHEDULED";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_COMPLETE = "COMPLETE";
    public static final String STATUS_FAILED = "FAILED";

    @Id
    private String id;

    // Email of the submitter who owns this upload, resolved from the bearer token at init time
    private String emailAddress;

    // Original client filename
    private String fileName;

    // Absolute path of the assembled file under mona.uploads
    @Type(type = "org.hibernate.type.TextType")
    private String storedPath;

    // msp, mgf or massbank, null until detected from the extension
    private String format;

    // Total bytes the client promised at init, used to verify a complete assembly
    private Long fileSize;

    // Bytes committed to disk so far, drives the transfer progress bar and resume
    private Long uploadedBytes;

    // Total spectra counted after a cheap first pass, null until counted
    private Long total;

    // Spectra parsed out of the file so far
    private Long parsed;

    // Spectra successfully persisted
    private Long persisted;

    // Spectra that could not be parsed or saved and were skipped and logged
    private Long failed;

    // A Library is @OneToOne cascade=ALL/orphanRemoval on Spectrum, so each spectrum in a job gets
    // its own fresh Library row rather than sharing one. There is therefore no single library id to
    // record on the job, only the user supplied name below

    // User supplied name for the library this upload creates
    private String libraryName;

    private String libraryDescription;

    private String libraryLink;

    // Prefix used to generate sequential per spectrum ids (PREFIX000001, PREFIX000002, ...),
    // Null means use the default auto generated spectrum id
    private String libraryPrefix;

    // Optional override attributing every spectrum in this upload to someone other than the
    // uploading account, mirroring the interactive library form's Submitter fields
    private String librarySubmitterEmail;

    private String librarySubmitterFirstName;

    private String librarySubmitterLastName;

    private String librarySubmitterInstitution;

    // JSON encoded array of extra tag text strings from the library form's additional tags input
    @Type(type = "org.hibernate.type.TextType")
    private String additionalTags;

    private Date date;

    private Date lastUpdated;

    private String status;

    @Type(type = "org.hibernate.type.TextType")
    private String errorMessage;

    public UploadJob() {
        this.uploadedBytes = 0L;
        this.parsed = 0L;
        this.persisted = 0L;
        this.failed = 0L;
    }

    public UploadJob(String id, String emailAddress, String fileName, String storedPath, String format, Long fileSize, String libraryName, Date date, String status) {
        this.id = id;
        this.emailAddress = emailAddress;
        this.fileName = fileName;
        this.storedPath = storedPath;
        this.format = format;
        this.fileSize = fileSize;
        this.libraryName = libraryName;
        this.date = date;
        this.lastUpdated = date;
        this.status = status;
        this.uploadedBytes = 0L;
        this.parsed = 0L;
        this.persisted = 0L;
        this.failed = 0L;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStoredPath() {
        return storedPath;
    }

    public void setStoredPath(String storedPath) {
        this.storedPath = storedPath;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getUploadedBytes() {
        return uploadedBytes;
    }

    public void setUploadedBytes(Long uploadedBytes) {
        this.uploadedBytes = uploadedBytes;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getParsed() {
        return parsed;
    }

    public void setParsed(Long parsed) {
        this.parsed = parsed;
    }

    public Long getPersisted() {
        return persisted;
    }

    public void setPersisted(Long persisted) {
        this.persisted = persisted;
    }

    public Long getFailed() {
        return failed;
    }

    public void setFailed(Long failed) {
        this.failed = failed;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public String getLibraryDescription() {
        return libraryDescription;
    }

    public void setLibraryDescription(String libraryDescription) {
        this.libraryDescription = libraryDescription;
    }

    public String getLibraryLink() {
        return libraryLink;
    }

    public void setLibraryLink(String libraryLink) {
        this.libraryLink = libraryLink;
    }

    public String getLibraryPrefix() {
        return libraryPrefix;
    }

    public void setLibraryPrefix(String libraryPrefix) {
        this.libraryPrefix = libraryPrefix;
    }

    public String getLibrarySubmitterEmail() {
        return librarySubmitterEmail;
    }

    public void setLibrarySubmitterEmail(String librarySubmitterEmail) {
        this.librarySubmitterEmail = librarySubmitterEmail;
    }

    public String getLibrarySubmitterFirstName() {
        return librarySubmitterFirstName;
    }

    public void setLibrarySubmitterFirstName(String librarySubmitterFirstName) {
        this.librarySubmitterFirstName = librarySubmitterFirstName;
    }

    public String getLibrarySubmitterLastName() {
        return librarySubmitterLastName;
    }

    public void setLibrarySubmitterLastName(String librarySubmitterLastName) {
        this.librarySubmitterLastName = librarySubmitterLastName;
    }

    public String getLibrarySubmitterInstitution() {
        return librarySubmitterInstitution;
    }

    public void setLibrarySubmitterInstitution(String librarySubmitterInstitution) {
        this.librarySubmitterInstitution = librarySubmitterInstitution;
    }

    public String getAdditionalTags() {
        return additionalTags;
    }

    public void setAdditionalTags(String additionalTags) {
        this.additionalTags = additionalTags;
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadJob that = (UploadJob) o;
        return Objects.equals(id, that.id) && Objects.equals(emailAddress, that.emailAddress) && Objects.equals(fileName, that.fileName) && Objects.equals(storedPath, that.storedPath) && Objects.equals(format, that.format) && Objects.equals(fileSize, that.fileSize) && Objects.equals(uploadedBytes, that.uploadedBytes) && Objects.equals(total, that.total) && Objects.equals(parsed, that.parsed) && Objects.equals(persisted, that.persisted) && Objects.equals(failed, that.failed) && Objects.equals(libraryName, that.libraryName) && Objects.equals(libraryDescription, that.libraryDescription) && Objects.equals(libraryLink, that.libraryLink) && Objects.equals(libraryPrefix, that.libraryPrefix) && Objects.equals(librarySubmitterEmail, that.librarySubmitterEmail) && Objects.equals(librarySubmitterFirstName, that.librarySubmitterFirstName) && Objects.equals(librarySubmitterLastName, that.librarySubmitterLastName) && Objects.equals(librarySubmitterInstitution, that.librarySubmitterInstitution) && Objects.equals(additionalTags, that.additionalTags) && Objects.equals(date, that.date) && Objects.equals(lastUpdated, that.lastUpdated) && Objects.equals(status, that.status) && Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, emailAddress, fileName, storedPath, format, fileSize, uploadedBytes, total, parsed, persisted, failed, libraryName, libraryDescription, libraryLink, libraryPrefix, librarySubmitterEmail, librarySubmitterFirstName, librarySubmitterLastName, librarySubmitterInstitution, additionalTags, date, lastUpdated, status, errorMessage);
    }
}
