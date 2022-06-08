package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain;

import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

public class News implements Serializable {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "news_id")
    @SequenceGenerator(name = "news_id", initialValue = 1, allocationSize = 50)
    @Id
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "submitted", nullable = false)
    private Date submitted;

    private String title;

    private String content;

    @PrePersist
    protected void onCreate() {
        submitted = new Date();
    }

    public News() {}

    public News(Long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Date submitted) {
        this.submitted = submitted;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
