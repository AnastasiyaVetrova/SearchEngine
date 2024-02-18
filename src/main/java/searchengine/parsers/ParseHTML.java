package searchengine.parsers;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.util.TreeSet;

public class ParseHTML {

    public TreeSet<PageEntity> getParseUrl(PageEntity page, SiteEntity siteEntity) {
        TreeSet<PageEntity> treeSetUrl = new TreeSet<>();
        String regex1 = "https?://(www.)?";
        String baseUrl =siteEntity.getUrl().replaceAll(regex1,"");
//        String pageUrl = page.getPath();
        Connection connection = Jsoup.connect(page.getPath());

        try {
            Document document = connection.get();
            page.setCode(connection.response().statusCode());
            Elements elements = document.select("a[href]");

            for (Element element : elements) {
                String elemUrl = element.absUrl("href");
                boolean isRegexUrl = elemUrl.contains(baseUrl)
                        && elemUrl.matches("[^#]+") &&
                        !elemUrl.endsWith("pdf");
                if (!isRegexUrl) {
                    continue;
                }
                PageEntity pageEntity = new PageEntity();
                pageEntity.setPath(elemUrl);
                pageEntity.setContent(Jsoup.connect(elemUrl).get().toString());
                pageEntity.setSite(siteEntity);
                treeSetUrl.add(pageEntity);
            }
        } catch (IOException e) {
//            String body = connection.response().body();
//            if (body.isEmpty()) {connection.response().statusCode()
//                page.setCode(500);
//            } else {
            page.setCode(500);
            page.setContent(e.toString());
            treeSetUrl.add(page);
            e.printStackTrace();

        } catch (Exception e) {
            page.setCode(connection.response().statusCode());
            page.setContent(e.toString());
            treeSetUrl.add(page);
            e.printStackTrace();
        }
        return treeSetUrl;
    }
}
