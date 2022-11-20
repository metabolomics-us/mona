//package edu.ucdavis.fiehnlab.mona.backend.core.domain.views;
//
//import org.hibernate.annotations.Immutable;
//import org.hibernate.annotations.Subselect;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.Id;
//
//@Entity
//@Subselect("select * from search_table_mat")
//@Immutable
//public class SearchTable {
//    @Id
//    private String monaId;
//
//    @Column(name = "metadata_name")
//    private String metadataName;
//
//    @Column(name = "metadata_unit")
//    private String metadataUnit;
//
//    @Column(name = "metadata_value")
//    private String metadataValue;
//
//    @Column(name = "metadata_hidden")
//    private String metadataHidden;
//
//    @Column(name = "metadata_category")
//    private String metadataCategory;
//
//    @Column(name = "metadata_computed")
//    private String metadataComputed;
//
//    @Column(name = "compound_kind")
//    private String compoundKind;
//
//    @Column(name = "compound_mol_file")
//    private String compoundMolFile;
//
//    @Column(name = "compound_computed")
//    private String compoundComputed;
//
//    @Column(name = "inchi")
//    private String inchi;
//
//    @Column(name = "inchikey")
//    private String inchikey;
//
//    @Column(name = "last_name")
//    private String lastName;
//
//    @Column(name = "first_name")
//    private String firstName;
//
//    @Column(name = "institution")
//    private String institution;
//
//    @Column(name = "email_address")
//    private String emailAddress;
//
//    @Column(name = "block1")
//    private String block1;
//
//    @Column(name = "block2")
//    private String block2;
//
//    @Column(name = "block3")
//    private String block3;
//
//    @Column(name = "splash")
//    private String splash;
//
//    @Column(name = "text")
//    private String text;
//
//    @Column(name = "rule_based")
//    private String ruleBased;
//
//    public String getMonaId() {
//        return monaId;
//    }
//
//    public String getMetadataName() {
//        return metadataName;
//    }
//
//    public String getMetadataUnit() {
//        return metadataUnit;
//    }
//
//    public String getMetadataValue() {
//        return metadataValue;
//    }
//
//    public String getMetadataHidden() {
//        return metadataHidden;
//    }
//
//    public String getMetadataCategory() {
//        return metadataCategory;
//    }
//
//    public String getMetadataComputed() {
//        return metadataComputed;
//    }
//
//    public String getCompoundKind() {
//        return compoundKind;
//    }
//
//    public String getCompoundMolFile() {
//        return compoundMolFile;
//    }
//
//    public String getCompoundComputed() {
//        return compoundComputed;
//    }
//
//    public String getInchi() {
//        return inchi;
//    }
//
//    public String getInchikey() {
//        return inchikey;
//    }
//
//    public String getLastName() {
//        return lastName;
//    }
//
//    public String getFirstName() {
//        return firstName;
//    }
//
//    public String getInstitution() {
//        return institution;
//    }
//
//    public String getEmailAddress() {
//        return emailAddress;
//    }
//
//    public String getBlock1() {
//        return block1;
//    }
//
//    public String getBlock2() {
//        return block2;
//    }
//
//    public String getBlock3() {
//        return block3;
//    }
//
//    public String getSplash() {
//        return splash;
//    }
//
//    public String getText() {
//        return text;
//    }
//
//    public String getRuleBased() {
//        return ruleBased;
//    }
//}
