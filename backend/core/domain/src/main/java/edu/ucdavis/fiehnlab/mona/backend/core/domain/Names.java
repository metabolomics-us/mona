package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "name")
@Profile({"mona.persistence"})
public class Names implements Serializable {
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "names_count")
    @SequenceGenerator(name = "names_count", initialValue = 1, allocationSize = 200)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Compound compoundNames;

    private Boolean computed;

    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "name")
    private String name;

    private Double score;

    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "source")
    private String source;

    public Names() {
    }

    public Names(Boolean computed, String name, Double score, String source) {
        this.computed = computed;
        this.name = name;
        this.score = score;
        this.source = source;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getComputed() {
        return computed;
    }

    public String getName() {
        return name;
    }

    public Double getScore() {
        return score;
    }

    public String getSource() { return source;}

    public void setComputed(Boolean computed) {
        this.computed = computed;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Names names = (Names) o;
        return Objects.equals(id, names.id) && Objects.equals(compoundNames, names.compoundNames) && Objects.equals(computed, names.computed) && Objects.equals(name, names.name) && Objects.equals(score, names.score) && Objects.equals(source, names.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, compoundNames, computed, name, score, source);
    }
}
