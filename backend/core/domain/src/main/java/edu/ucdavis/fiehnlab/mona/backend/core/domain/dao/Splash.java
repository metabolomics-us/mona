package edu.ucdavis.fiehnlab.mona.backend.core.domain.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "splash")
@Profile({"mona.persistence"})
public class Splash implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "splash_id")
    @SequenceGenerator(name = "splash_id", initialValue = 1, allocationSize = 50)
    @JsonIgnore
    private Long id;

    @OneToOne(mappedBy = "splash")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Spectrum spectrum;

    private String block1;
    private String block2;
    private String block3;
    private String block4;
    private String splash;

    public Splash() {
    }

    public Splash(String block1, String block2, String block3, String block4, String splash) {
        this.block1 = block1;
        this.block2 = block2;
        this.block3 = block3;
        this.block4 = block4;
        this.splash = splash;
    }

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

    public String getBlock1() {
        return block1;
    }

    public void setBlock1(String block1) {
        this.block1 = block1;
    }

    public String getBlock2() {
        return block2;
    }

    public void setBlock2(String block2) {
        this.block2 = block2;
    }

    public String getBlock3() {
        return block3;
    }

    public void setBlock3(String block3) {
        this.block3 = block3;
    }

    public String getBlock4() {
        return block4;
    }

    public void setBlock4(String block4) {
        this.block4 = block4;
    }

    public String getSplash() {
        return splash;
    }

    public void setSplash(String splash) {
        this.splash = splash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Splash splash1 = (Splash) o;
        return id.equals(splash1.id) && spectrum.equals(splash1.spectrum) && Objects.equals(block1, splash1.block1) && Objects.equals(block2, splash1.block2) && Objects.equals(block3, splash1.block3) && Objects.equals(block4, splash1.block4) && Objects.equals(splash, splash1.splash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, spectrum, block1, block2, block3, block4, splash);
    }
}
