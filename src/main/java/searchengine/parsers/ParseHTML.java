package searchengine.parsers;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.controllers.ApiController;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.regex.BaseRegex;

import java.io.IOException;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;

public class ParseHTML {

    public TreeSet<PageEntity> getParseUrl(PageEntity page, SiteEntity siteEntity) {
        TreeSet<PageEntity> treeSetUrl = new TreeSet<>();
        String baseUrlRegex = BaseRegex.getBaseUrl(siteEntity);
        String urlRegex = baseUrlRegex + "[^#\\s]+";

        if (page.getCode() != 200) {
            return treeSetUrl;
        }
        Document document = Jsoup.parse(page.getContent(),siteEntity.getUrl());
        Elements elements = document.select("a[href]");

        for (Element element : elements) {
            if (ApiController.isIndexingEnd()) {
                throw new CancellationException();
            }
            try {
                Thread.sleep(150);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            String elemUrl = element.absUrl("href");
            boolean isRegexUrl = elemUrl.matches(urlRegex) && !isFile(elemUrl);
            if (!isRegexUrl) {
                continue;
            }
            PageEntity pageEntity = new PageEntity();
            pageEntity.setPath(elemUrl.replaceAll(baseUrlRegex, ""));
            Connection connection = Jsoup.connect(elemUrl);
            try {
                pageEntity.setContent(connection.get().toString());
                pageEntity.setCode(connection.response().statusCode());
            } catch (IOException e) {
                page.setCode(500);
                page.setContent(e.toString());
                treeSetUrl.add(page);

            } catch (Exception e) {
                page.setCode(connection.response().statusCode());
                page.setContent(e.toString());
                treeSetUrl.add(page);

            } finally {
                pageEntity.setSite(siteEntity);
                treeSetUrl.add(pageEntity);
            }
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
                || path.contains("?")
                || path.contains("sort");
    }
}
