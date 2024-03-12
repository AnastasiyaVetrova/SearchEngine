package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {
    @Query("select l FROM LemmaEntity l where l.lemma =:lemma and l.site =:site")
    LemmaEntity findByLemmaAndSite(@Param("lemma") String lemma, @Param("site") SiteEntity site);
    @Query("select count(l)>0 FROM LemmaEntity l where l.lemma =:lemma and l.site =:site")
    boolean existsByLemmaAndSite(@Param("lemma") String lemma, @Param("site") SiteEntity site);
    @Modifying
    @Transactional
    @Query("update LemmaEntity l set l.frequency=:frequency where l.id=:id")
    void updateLemma (@Param("id") Integer id, @Param("frequency") Integer frequency);

}
