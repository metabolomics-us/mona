package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladmihalcea.hibernate.type.json.JsonType;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@IdClass(SpectrumResultId.class)
@Table(name = "spectrum_result")
@TypeDef(
        name = "json",
        typeClass = JsonType.class
)
@Profile({"mona.persistence"})
public class SpectrumResult implements Serializable {
    //can be standalone primary key
    @Id
    @JsonProperty
    private String monaId;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    @JsonProperty
    private Spectrum spectrum;

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
        return Objects.equals(monaId, that.monaId) && Objects.equals(spectrum, that.spectrum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monaId, spectrum);
    }
}
