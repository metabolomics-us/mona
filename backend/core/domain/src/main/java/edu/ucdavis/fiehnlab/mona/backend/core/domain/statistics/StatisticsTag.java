package edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics;

import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.util.Objects;

@Entity
@IdClass(StatisticsTagId.class)
@Table(name = "statistics_tags")
@Profile({"mona.persistence"})
public class StatisticsTag {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "statistics_tags_id")
    @SequenceGenerator(name = "statistics_tags_id", initialValue = 1, allocationSize = 50)
    @Id
    private Long id;

    @Id
    private String text;

    private Boolean ruleBased;

    private Integer count;

    private String category;

    public StatisticsTag() {
    }

    public StatisticsTag(String text, Boolean ruleBased, Integer count, String category) {
        this.text = text;
        this.ruleBased = ruleBased;
        this.count = count;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getRuleBased() {
        return ruleBased;
    }

    public void setRuleBased(Boolean ruleBased) {
        this.ruleBased = ruleBased;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatisticsTag that = (StatisticsTag) o;
        return Objects.equals(id, that.id) && Objects.equals(text, that.text) && Objects.equals(ruleBased, that.ruleBased) && Objects.equals(count, that.count) && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, ruleBased, count, category);
    }
}
