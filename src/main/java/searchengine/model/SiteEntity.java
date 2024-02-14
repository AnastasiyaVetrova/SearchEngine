package searchengine.model;


import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "site")
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "status", columnDefinition = "enum('INDEXING', 'INDEXED', 'FAILED')", nullable = false)
    @Enumerated(EnumType.STRING)
    private EnumStatus status;
    @Column(name = "status_time", nullable = false)
    @UpdateTimestamp
    private LocalDateTime statusTime;
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String error;
    @Column(name = "url", columnDefinition = "VARCHAR(255)", nullable = false)
    private String url;
    @Column(name = "name", columnDefinition = "VARCHAR(255)", nullable = false)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "site_id")
    private Set<PageEntity> page;

}
