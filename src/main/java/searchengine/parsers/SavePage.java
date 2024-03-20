package searchengine.parsers;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;


@Data
@AllArgsConstructor
public class SavePage {
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    @Transactional
    public boolean findPageToDB(PageEntity page, SiteEntity siteEntity) {
        return pageRepository.existsByPath(page.getPath(), siteEntity);
    }
    @Transactional
    public void savePageToDB (PageEntity pageEntity, SiteEntity siteEntity) {
        pageRepository.save(pageEntity);
        siteRepository.save(siteEntity);
    }

//    @Transactional
//    public void savePageToDB (TreeSet<PageEntity> pageEntities, SiteEntity siteEntity) {
//        pageRepository.saveAll(pageEntities);
//        siteRepository.save(siteEntity);
//    }
}
