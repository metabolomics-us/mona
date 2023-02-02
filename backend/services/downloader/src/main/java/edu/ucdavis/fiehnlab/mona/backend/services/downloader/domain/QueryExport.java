package edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain;

import org.hibernate.annotations.Type;
import org.springframework.context.annotation.Profile;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "query_export")
@Profile({"mona.persistence.downloader"})
public class QueryExport implements Serializable {
    @Id
    private String id;

    @Type(type = "org.hibernate.type.TextType")
    private String label;

    @Type(type = "org.hibernate.type.TextType")
    private String query;

    @Type(type = "org.hibernate.type.TextType")
    private String format;

    private String emailAddress;

    private Date date;

    private Long count;

    private Long size;

    @Type(type = "org.hibernate.type.TextType")
    private String queryFile;

    @Type(type = "org.hibernate.type.TextType")
    private String exportFile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
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

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getQueryFile() {
        return queryFile;
    }

    public void setQueryFile(String queryFile) {
        this.queryFile = queryFile;
    }

    public String getExportFile() {
        return exportFile;
    }

    public void setExportFile(String exportFile) {
        this.exportFile = exportFile;
    }

    public QueryExport(String id, String label, String query, String format, String emailAddress, Date date, Long count, Long size, String queryFile, String exportFile) {
        this.id = id;
        this.label = label;
        this.query = query;
        this.format = format;
        this.emailAddress = emailAddress;
        this.date = date;
        this.count = count;
        this.size = size;
        this.queryFile = queryFile;
        this.exportFile = exportFile;
    }

    public QueryExport() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryExport that = (QueryExport) o;
        return Objects.equals(id, that.id) && Objects.equals(label, that.label) && Objects.equals(query, that.query) && Objects.equals(format, that.format) && Objects.equals(emailAddress, that.emailAddress) && Objects.equals(date, that.date) && Objects.equals(count, that.count) && Objects.equals(size, that.size) && Objects.equals(queryFile, that.queryFile) && Objects.equals(exportFile, that.exportFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, query, format, emailAddress, date, count, size, queryFile, exportFile);
    }
}
