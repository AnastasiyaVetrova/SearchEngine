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
        String pageUrl = page.getPath().contains(siteEntity.getUrl()) ? page.getPath() :
                siteEntity.getUrl().concat(page.getPath());
//        String pageUrl =siteEntity.getUrl().concat(page.getPath());
//        String pageUrl = page.getPath();
        Connection connection = Jsoup.connect(pageUrl);

        try {
            Document document = connection.get();
            page.setCode(connection.response().statusCode());
            Elements elements = document.select("a[href]");

            for (Element element : elements) {
                String elemUrl = element.attr("href");
                boolean isRegexUrl =  elemUrl.startsWith("/")
                        && elemUrl.matches("[^#]+") &&
                        !elemUrl.endsWith("pdf")&&
                        !elemUrl.endsWith("jpg");
                if (!isRegexUrl) {
                    continue;
                }
                PageEntity pageEntity = new PageEntity();
                pageEntity.setPath(elemUrl);
//                String path = siteEntity.getUrl().concat(elemUrl);
                Connection connection1 = Jsoup.connect(siteEntity.getUrl().concat(elemUrl));
                pageEntity.setContent(connection1.get().toString());
                pageEntity.setCode(connection.response().statusCode());
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
