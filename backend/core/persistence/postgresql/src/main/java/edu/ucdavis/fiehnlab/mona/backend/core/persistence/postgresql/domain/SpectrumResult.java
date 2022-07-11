package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain;

import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Entity
@IdClass(SpectrumResultId.class)
@Table(name = "spectrum_result")
@TypeDef(
        name = "json",
        typeClass = JsonType.class
)
public class SpectrumResult {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "spectrum_result_id")
    @SequenceGenerator(name = "spectrum_result_id", initialValue = 1, allocationSize = 50)
    @Id
    private Long id;

    //can be standalone primary key
    @Id
    private String monaId;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMonaId() {
        return monaId;
    }

    public void setMonaId(String monaId) {
        this.monaId = monaId;
    }

    public SpectrumResult(){}

    public SpectrumResult(String monaId, String content) {
        this.monaId = monaId;
        this.content = content;
    }

}
