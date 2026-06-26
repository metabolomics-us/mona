package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import org.hibernate.annotations.Type;
import org.springframework.context.annotation.Profile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Per skeleton ClassyFire classification cache. Keyed by the 14 character InChIKey first block (the
 * connectivity layer) so a structure is only ever sent to ClassyFire once and the result is reused for every
 * spectrum sharing that skeleton. The classification payload is the serialized List of classification MetaData
 */
@Entity
@Table(name = "classification_cache")
@Profile({"mona.persistence"})
public class ClassificationCache implements Serializable {

    @Id
    @Column(name = "inchikey_block", length = 14)
    private String inchikeyBlock;

    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "classification")
    private String classification;

    @Column(name = "created")
    private Date created;

    public ClassificationCache() {
    }

    public ClassificationCache(String inchikeyBlock, String classification, Date created) {
        this.inchikeyBlock = inchikeyBlock;
        this.classification = classification;
        this.created = created;
    }

    public String getInchikeyBlock() {
        return inchikeyBlock;
    }

    public void setInchikeyBlock(String inchikeyBlock) {
        this.inchikeyBlock = inchikeyBlock;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
