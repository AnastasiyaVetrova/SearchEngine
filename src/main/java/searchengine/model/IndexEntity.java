//package searchengine.model;
//
//import lombok.Data;
//
//import javax.persistence.*;
//@Entity
//@Table(name = "index")
//@Data
//public class IndexEntity {
//
//        @Id
//        @GeneratedValue(strategy = GenerationType.AUTO)
//        @Column(name = "id", nullable = false)
//        private int id;
//        @OneToOne
//        @JoinColumn(name = "page_id", nullable = false)
//        private PageEntity page;
//        @OneToMany
//        @JoinColumn(name = "lemma_id", nullable = false)
//        private LemmaEntity lemma;
//        @Column(name = "rank", nullable = false)
//        private float rank;
//}
