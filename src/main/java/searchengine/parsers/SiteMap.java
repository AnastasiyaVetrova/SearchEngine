package searchengine.parsers;

import lombok.AllArgsConstructor;
import searchengine.controllers.ApiController;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RecursiveAction;

@AllArgsConstructor
public class SiteMap extends RecursiveAction {
    private final PageEntity pageEntity;
    private final SavePage savePage;
    private final SiteEntity siteEntity;
    private final SaveLemmaAndIndex saveLemmaAndIndex;

    @Override
    protected void compute() {
        ParseHTML parseHTML = new ParseHTML();
        TreeSet<PageEntity> allUrl = parseHTML.getParseUrl(pageEntity, siteEntity);

        FindLemma findLemma = new FindLemma(saveLemmaAndIndex);
        List<SiteMap> siteLink = new ArrayList<>();

        for (PageEntity page : allUrl) {
            if (ApiController.isIndexingEnd()) {
                throw new CancellationException();
            }
            if (savePage.findPageToDB(page, siteEntity)) {
                continue;
            }
            savePage.savePageToDB(page, siteEntity);
            findLemma.findLemmaOnPage(page);
            SiteMap saveSiteMap = new SiteMap(page, savePage, siteEntity, saveLemmaAndIndex);
            saveSiteMap.fork();
            siteLink.add(saveSiteMap);
        }
        for (SiteMap map : siteLink) {
            map.join();
        }
    }
}
//        TreeSet<PageEntity> resultUrl = new TreeSet<>();
//        if (!resultUrl.isEmpty()) {
//            savePage.savePageToDB(resultUrl, siteEntity);
//        }

//        resultUrl.forEach(findLemma::findLemmaOnPage);
//            resultUrl.add(page);
