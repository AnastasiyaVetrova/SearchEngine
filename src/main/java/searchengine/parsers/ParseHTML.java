package searchengine.parsers;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;

import java.io.IOException;
import java.util.TreeSet;

public class ParseHTML {

    public TreeSet<PageEntity> getParseUrl(PageEntity page) {
        TreeSet<PageEntity> treeSetUrl = new TreeSet<>();
        String pageUrl = page.getPath();
        Connection connection = Jsoup.connect(page.getPath());

        try {
            Document document = connection.get();
            page.setCode(connection.response().statusCode());
            Elements elements = document.select("a[href]");

            for (Element element : elements) {
                String elemUrl = element.absUrl("href");
                boolean isRegexUrl = elemUrl.contains(pageUrl)
                        && elemUrl.matches("[^#]+") &&
                        !elemUrl.endsWith("pdf");
                if (!isRegexUrl) {
                    continue;
                }
                PageEntity pageEntity = new PageEntity();
                pageEntity.setPath(elemUrl);
                pageEntity.setContent(Jsoup.connect(elemUrl).get().toString());
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

        } catch (Exception e) {
            page.setCode(connection.response().statusCode());
            page.setContent(e.toString());
            treeSetUrl.add(page);
        }
        return treeSetUrl;
    }
}
