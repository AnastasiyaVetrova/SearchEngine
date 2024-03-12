package searchengine.model;


import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "page", uniqueConstraints = {@UniqueConstraint(columnNames = {"site", "path"})})
public class PageEntity implements Comparable<PageEntity> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @ManyToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinColumn(name = "site", nullable = false)
    private SiteEntity site;
    @Column(name = "path", columnDefinition = "VARCHAR(255)", nullable = false)
    @EqualsAndHashCode.Include
    private String path;
    @Column(name = "code", nullable = false)
    private Integer code;
    @Column(name = "content", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "page", fetch = FetchType.LAZY)
    private List<IndexEntity> indexEntity;

    @Override
    public int compareTo(PageEntity o) {
        return this.getPath().compareTo(o.getPath());
    }
}
