package edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain;

import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "predefined_queries")
@Profile({"mona.persistence.downloader"})
public class PredefinedQuery implements Serializable {
    @Id
    private String label;

    private String description;

    private String query;

    private Long queryCount;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "json_export_id")
    private QueryExport jsonExport;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "msp_export_id")
    private QueryExport mspExport;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sdf_export_id")
    private QueryExport sdfExport;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Long getQueryCount() {
        return queryCount;
    }

    public void setQueryCount(Long queryCount) {
        this.queryCount = queryCount;
    }

    public QueryExport getJsonExport() {
        return jsonExport;
    }

    public void setJsonExport(QueryExport jsonExport) {
        this.jsonExport = jsonExport;
    }

    public QueryExport getMspExport() {
        return mspExport;
    }

    public void setMspExport(QueryExport mspExport) {
        this.mspExport = mspExport;
    }

    public QueryExport getSdfExport() {
        return sdfExport;
    }

    public void setSdfExport(QueryExport sdfExport) {
        this.sdfExport = sdfExport;
    }

    public PredefinedQuery(String label, String description, String query, Long queryCount, QueryExport jsonExport, QueryExport mspExport, QueryExport sdfExport) {
        this.label = label;
        this.description = description;
        this.query = query;
        this.queryCount = queryCount;
        this.jsonExport = jsonExport;
        this.mspExport = mspExport;
        this.sdfExport = sdfExport;
    }

    public PredefinedQuery(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PredefinedQuery that = (PredefinedQuery) o;
        return Objects.equals(label, that.label) && Objects.equals(description, that.description) && Objects.equals(query, that.query) && Objects.equals(queryCount, that.queryCount) && Objects.equals(jsonExport, that.jsonExport) && Objects.equals(mspExport, that.mspExport) && Objects.equals(sdfExport, that.sdfExport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, description, query, queryCount, jsonExport, mspExport, sdfExport);
    }
}
