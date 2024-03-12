package searchengine.model;

import lombok.Data;

import javax.persistence.*;
@Data
@Entity
@Table(name = "indexes")

public class IndexEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Integer id;
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "page_id", nullable = false)
        private PageEntity page;
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "lemma_id", nullable = false)
        private LemmaEntity lemma;
        @Column(name = "lemma_rank", nullable = false)
        private float lemmaRank;//количество данной леммы для данной страницы
}
