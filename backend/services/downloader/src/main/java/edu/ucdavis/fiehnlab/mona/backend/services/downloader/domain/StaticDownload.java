package edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain;

import org.springframework.context.annotation.Profile;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "static_download")
@Profile({"mona.persistence.downloader"})
public class StaticDownload implements Serializable {
    @Id
    private String fileName;

    private String category;

    private String description;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StaticDownload() {

    }

    public StaticDownload(String fileName, String category, String description) {
        this.fileName = fileName;
        this.category = category;
        this.description = description;
    }

    public StaticDownload(String fileName) {
        String[] path = fileName.split("/");

        if (path.length == 1) {
            this.fileName = fileName;
            this.category = null;
        } else {
            this.fileName = path[path.length - 1];
            this.category = path[0];
        }
        this.description = null;
    }

    public StaticDownload(String fileName, String description) {
        String[] path = fileName.split("/");

        if (path.length == 1) {
            this.fileName = fileName;
            this.category = null;
        } else {
            this.fileName = path[path.length - 1];
            this.category = path[0];
        }
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaticDownload that = (StaticDownload) o;
        return Objects.equals(fileName, that.fileName) && Objects.equals(category, that.category) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, category, description);
    }
}
