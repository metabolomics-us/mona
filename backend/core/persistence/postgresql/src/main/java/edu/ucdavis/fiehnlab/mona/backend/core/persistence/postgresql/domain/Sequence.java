package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.sql.rowset.serial.SerialArray;
import java.io.Serializable;

@Entity
@Table(name = "sequence")
public class Sequence implements Serializable {
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
