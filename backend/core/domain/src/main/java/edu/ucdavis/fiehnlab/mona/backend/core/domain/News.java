package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "news")
@Profile({"mona.persistence"})
public class News {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "news_id")
    @SequenceGenerator(name = "news_id", initialValue = 1, allocationSize = 50)
    @Id
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "submitted", nullable = false)
    private Date submitted = new Date();

    private String title;

    private String content;

    public News() {}

    public News(Long id, String title, String content) {
        this.id = id;
        this.submitted = new Date();
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
