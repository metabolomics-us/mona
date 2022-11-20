//package edu.ucdavis.fiehnlab.mona.backend.core.domain.views;
//
//import org.hibernate.annotations.Immutable;
//import org.hibernate.annotations.Subselect;
//
//import javax.persistence.Entity;
//import javax.persistence.Id;
//
//@Entity
//@Immutable
//@Subselect("select sr.mona_id, m.name, m.unit, m.value, m.hidden, m.category, m.computed" +
//        "from spectrum_result sr" +
//        "INNER JOIN metadata m on sr.mona_id = m.mona_id")
//public class NewSearchTable {
//    @Id
//    private String monaId;
//}
