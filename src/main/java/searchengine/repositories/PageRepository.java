package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    boolean existsByPath(String path);
}
