package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    @Query("select count(p)>0 FROM PageEntity p where p.path =:path and p.site= :site")
    boolean existsByPath(@Param("path") String path, @Param("site") SiteEntity site);
    PageEntity findByPathAndSite(String path, SiteEntity site);
    int countBySite(SiteEntity site);

}
