package searchengine.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;
@Data
@Entity
@Table(name = "lemma", uniqueConstraints = {@UniqueConstraint(columnNames = {"site", "lemma"})})
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @ManyToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinColumn(name = "site", nullable = false)
    private SiteEntity site;
    @Column(name = "lemma", columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;
    @Version
    @Column(name = "frequency", nullable = false)//количество страниц, на которых слово встречается хотя бы один раз.
    private int frequency;
    @OneToMany(mappedBy = "lemma", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<IndexEntity> indexEntity;
}
