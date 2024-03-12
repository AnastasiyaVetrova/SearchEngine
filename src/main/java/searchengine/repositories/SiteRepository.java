package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteEntity;
@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {
    @Transactional
    void deleteByUrl(String url);
    boolean existsByUrl(String url);
    SiteEntity findByUrl(String url);

}
