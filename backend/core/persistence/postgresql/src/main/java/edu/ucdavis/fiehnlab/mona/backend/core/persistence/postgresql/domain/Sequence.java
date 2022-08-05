package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sequence")
public class Sequence {
    @Id
    private String id;

    private Integer value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Sequence() {}

    public Sequence(String id, Integer value) {
        this.id = id;
        this.value = value;
    }
}
