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
        String baseUrlRegex = BaseUrlRegex.getBaseUrl(siteEntity);
        String urlRegex = baseUrlRegex + "[^#\\s]+";
        String pageUrl = page.getPath().contains(siteEntity.getUrl()) ? page.getPath() :
                siteEntity.getUrl().concat(page.getPath());
        Connection connection = Jsoup.connect(pageUrl);
        try {
            Document document = connection.get();
//            page.setCode(connection.response().statusCode());
            Elements elements = document.select("a[href]");

            for (Element element : elements) {
                String elemUrl = element.absUrl("href");
                boolean isRegexUrl = elemUrl.matches(urlRegex) && !isFile(elemUrl);

                if (!isRegexUrl) {
                    continue;
                }
                PageEntity pageEntity = new PageEntity();
                pageEntity.setPath(elemUrl.replaceAll(baseUrlRegex, ""));
                Connection connection1 = Jsoup.connect(elemUrl);
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

    private boolean isFile(String path) {
        return path.contains(".jpg")
                || path.contains(".jpeg")
                || path.contains(".png")
                || path.contains(".gif")
                || path.contains(".webp")
                || path.contains(".pdf")
                || path.contains(".eps")
                || path.contains(".xlsx")
                || path.contains(".doc")
                || path.contains(".pptx")
                || path.contains(".docx")
                || path.contains("?");
    }
}
////        String pageUrl = page.getPath().contains(siteEntity.getUrl()) ? page.getPath() :
////                siteEntity.getUrl().concat(page.getPath());
////        String pageUrl =siteEntity.getUrl().concat(page.getPath());
////        String pageUrl = page.getPath();
//        String regex="https?://(www.)?";
//        String res=siteEntity.getUrl().replaceAll(regex,"");
//
////        String baseUrlRegex="https?://(www.)?"+res;
//        String baseUrlRegex = "https?://(www.)?(?="+res+")[^#+\\s]+";
//                String path = siteEntity.getUrl().concat(elemUrl);