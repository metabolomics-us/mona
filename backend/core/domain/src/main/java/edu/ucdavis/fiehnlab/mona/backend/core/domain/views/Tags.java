//package edu.ucdavis.fiehnlab.mona.backend.core.domain.views;
//
//import org.hibernate.annotations.Immutable;
//import org.hibernate.annotations.Subselect;
//
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.IdClass;
//import java.util.Objects;
//
//@Entity
//@IdClass(TagsId.class)
//@Subselect("select * from tags")
//@Immutable
//public class Tags {
//    @Id
//    private String monaId;
//
//    @Id
//    private String text;
//
//    private Boolean ruleBased;
//
//    public Tags() {
//    }
//
//    public String getMonaId() {
//        return monaId;
//    }
//
//    public String getText() {
//        return text;
//    }
//
//    public Boolean getRuleBased() {
//        return ruleBased;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Tags tags = (Tags) o;
//        return Objects.equals(monaId, tags.monaId) && Objects.equals(text, tags.text) && Objects.equals(ruleBased, tags.ruleBased);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(monaId, text, ruleBased);
//    }
//}
