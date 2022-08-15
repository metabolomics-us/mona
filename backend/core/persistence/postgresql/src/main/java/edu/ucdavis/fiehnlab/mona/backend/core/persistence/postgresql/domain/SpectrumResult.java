package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonType;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao.Spectrum;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Objects;

@Entity
@IdClass(SpectrumResultId.class)
@Table(name = "spectrum_result")
@TypeDef(
        name = "json",
        typeClass = JsonType.class
)
public class SpectrumResult {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "spectrum_result_id")
    @JsonIgnore
    @SequenceGenerator(name = "spectrum_result_id", initialValue = 1, allocationSize = 50)
    @Id
    private Long id;

    //can be standalone primary key
    @Id
    private String monaId;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private Spectrum spectrum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Spectrum getSpectrum() {
        return spectrum;
    }

    public void setSpectrum(Spectrum spectrum) {
        this.spectrum = spectrum;
    }

    public String getMonaId() {
        return monaId;
    }

    public void setMonaId(String monaId) {
        this.monaId = monaId;
    }

    public SpectrumResult(){}

    public SpectrumResult(String monaId, Spectrum spectrum) {
        this.monaId = monaId;
        this.spectrum = spectrum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpectrumResult that = (SpectrumResult) o;
        return Objects.equals(id, that.id) && Objects.equals(monaId, that.monaId) && Objects.equals(spectrum, that.spectrum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, monaId, spectrum);
    }
}
