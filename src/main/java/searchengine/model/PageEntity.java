package searchengine.model;


import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "page", uniqueConstraints = {@UniqueConstraint(columnNames = {"site", "path"})})
//        , indexes = @Index(columnList ="id , path", name = "index_path"))
public class PageEntity implements Comparable<PageEntity> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity site;
    @Column(name = "path", columnDefinition = "VARCHAR(255)", nullable = false)
    @EqualsAndHashCode.Include
    private String path;
    @Column(name = "code", nullable = false)
    private Integer code;
    @Column(name = "content", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @Override
    public int compareTo(PageEntity o) {
        return this.getPath().compareTo(o.getPath());
    }
}
