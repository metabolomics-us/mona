package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import org.springframework.context.annotation.Profile;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Backs the atomic per prefix counter used to generate sequential custom spectrum ids
 * (PREFIX000001, PREFIX000002, ...) for library form uploads. Updates the id via a single atomic
 * "INSERT ... ON CONFLICT ... DO UPDATE ... RETURNING" statement, 
 * so this class exists only so Hibernate's ddl-auto creates the backing table
 */
@Entity
@Table(name = "library_prefix_counter")
@Profile({"mona.persistence"})
public class LibraryPrefixCounter implements Serializable {
    @Id
    private String prefix;

    private Long nextValue;

    public LibraryPrefixCounter() {
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Long getNextValue() {
        return nextValue;
    }

    public void setNextValue(Long nextValue) {
        this.nextValue = nextValue;
    }
}
