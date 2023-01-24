package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "spectrum_submitters")
@Immutable
public class SpectrumSubmitter {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "spectrum_submitter_id")
    @SequenceGenerator(name = "spectrum_submitter_id", initialValue = 1, allocationSize = 50)
    @JsonIgnore
    private Long id;

    @OneToOne(mappedBy = "submitter")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Spectrum spectrum;

    private String emailAddress;

    private String firstName;

    private String lastName;

    private String institution;

    public SpectrumSubmitter() {
    }

    public SpectrumSubmitter(String emailAddress, String firstName, String lastName, String institution) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.institution = institution;
        this.emailAddress = emailAddress;
    }

    public Spectrum getSpectrum() {
        return spectrum;
    }

    public void setSpectrum(Spectrum spectrum) {
        this.spectrum = spectrum;
    }

    public Long getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getInstitution() {
        return institution;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpectrumSubmitter that = (SpectrumSubmitter) o;
        return Objects.equals(id, that.id) && Objects.equals(spectrum, that.spectrum) && Objects.equals(emailAddress, that.emailAddress) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(institution, that.institution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, spectrum, emailAddress, firstName, lastName, institution);
    }

    @Override
    public String toString() {
        return "submitter = " +
                firstName + " " +
                lastName + " " +
                "(" + institution + ")";
    }
}
