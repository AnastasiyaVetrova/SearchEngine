package searchengine.parsers;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.Application;
import searchengine.exceptoin.ConnectingToPageException;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.regex.BaseRegex;
import searchengine.services.IndexingStartService;

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
        Document document = Jsoup.parse(page.getContent(), siteEntity.getUrl());
        Elements elements = document.select("a[href]");
        for (Element element : elements) {
            if (IndexingStartService.isIndexingEnd()) {
                throw new CancellationException();
            }
            try {
                Thread.sleep(500);
            } catch (Exception exception) {
                Application.LOG.error("Поток проснулся");
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
                new ConnectingToPageException(e.toString(), page, 500);
            } catch (Exception e) {
                new ConnectingToPageException(e.toString(), page, connection.response().statusCode());
            } finally {
                treeSetUrl.add(page);
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
