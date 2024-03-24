package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.Application;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.response.Message;
import searchengine.dto.response.MessageResponse;
import searchengine.exceptoin.ConnectingToSiteException;
import searchengine.model.EnumStatus;
import searchengine.model.IndexEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.parsers.FindLemma;
import searchengine.parsers.SaveLemmaAndIndex;
import searchengine.parsers.SavePage;
import searchengine.parsers.SiteMap;
import searchengine.regex.BaseRegex;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexingStartService implements IndexingService {
    private final SitesList sites;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private ExecutorService executorService;
    @Getter
    private static boolean isIndexingEnd = true;

    public MessageResponse startIndexing() {
        if (!isIndexingEnd) {
            return new Message(isIndexingEnd, "Индексация уже запущена");
        }
        isIndexingEnd = false;
        executorService = Executors.newCachedThreadPool();
        for (Site site : sites.getSites()) {
            Runnable task = () -> startOneSiteIndexing(site);
            executorService.execute(task);
        }
        executorService.shutdown();
        try {
            isIndexingEnd = executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (
                Exception exception) {
            return new Message(isIndexingEnd(), "Индексация остановлена принудительно");
        }
        return new Message(isIndexingEnd);
    }

    public void startOneSiteIndexing(Site site) {
        siteRepository.deleteByUrl(site.getUrl());
        SiteEntity siteEntity = createSite(site);
        siteRepository.save(siteEntity);
        PageEntity page;
        try {
            page = createPage(siteEntity.getUrl(), siteEntity);
        } catch (Exception exception) {
            new ConnectingToSiteException("Ошибка индексации: сайт не доступен: " + exception,
                    siteRepository, siteEntity);
            return;
        }
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        SavePage savePage = new SavePage(pageRepository, siteRepository);
        SaveLemmaAndIndex saveLemmaAndIndex = new SaveLemmaAndIndex(lemmaRepository, indexRepository);
        try {
            forkJoinPool.invoke(new SiteMap(page, savePage,
                    siteEntity, saveLemmaAndIndex));
            siteEntity.setStatus(EnumStatus.INDEXED);
            siteRepository.save(siteEntity);
        } catch (CancellationException exception) {
            forkJoinPool.shutdownNow();
            new ConnectingToSiteException("Индексация остановлена пользователем: " + exception,
                    siteRepository, siteEntity);
        } catch (Exception exception) {
            new ConnectingToSiteException("Ошибка в процессе индексации: " + exception,
                    siteRepository, siteEntity);
        }
    }

    public MessageResponse stopIndexing() {
        boolean isClose = false;
        if (isIndexingEnd) {
            return new Message(isClose, "Индексация не запущена");
        }
        isIndexingEnd = true;
        try {
            isClose = executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (Exception exception) {
            return new Message(isClose, "Остановить индексацию не удалось");
        }
        return new Message(isClose);
    }

    @Override
    public MessageResponse startIndexPage(String url) {
        isIndexingEnd = false;
        String regex = BaseRegex.getREGEX_URL() + "[^/]+";
        String urlPage = url.replaceAll(regex, "");
        String urlSite = url.replaceAll(urlPage, "");
        boolean isSite = sites.getSites().stream().map(Site::getUrl)
                .filter(s -> s.equals(urlSite))
                .anyMatch(s -> s.contains(urlSite));
        if (!isSite) {
            return new Message(false,
                    "Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        SiteEntity siteEntity = findSite(urlSite);
        PageEntity pageEntity;
        try {
            pageEntity = findPage(urlPage, siteEntity);
        } catch (Exception e) {
            return new Message(false,
                    "Данная страница недоступна");
        }
        SaveLemmaAndIndex saveLemmaAndIndex = new SaveLemmaAndIndex(lemmaRepository, indexRepository);
        FindLemma findLemma = new FindLemma(saveLemmaAndIndex);
        findLemma.findLemmaOnPage(pageEntity);
        siteEntity.setStatus(EnumStatus.INDEXED);
        siteRepository.save(siteEntity);
        isIndexingEnd = true;
        return new Message(true);
    }

    public SiteEntity findSite(String urlSite) {
        SiteEntity siteEntity;
        if (siteRepository.existsByUrl(urlSite)) {
            siteEntity = siteRepository.findByUrl(urlSite);
            siteEntity.setStatus(EnumStatus.INDEXING);
            siteRepository.save(siteEntity);
        } else {
            Site site = sites.getSites().stream()
                    .filter(s -> s.getUrl().contains(urlSite))
                    .findFirst().get();
            siteEntity = createSite(site);
            siteRepository.save(siteEntity);
        }
        return siteEntity;
    }

    public PageEntity findPage(String urlPage, SiteEntity siteEntity) throws Exception {
        if (pageRepository.existsByPath(urlPage, siteEntity)) {
            PageEntity page = pageRepository.findByPathAndSite(urlPage, siteEntity);
            page.getIndexEntity().stream()
                    .map(IndexEntity::getLemma)
                    .peek(l -> l.setFrequency(l.getFrequency() - 1))
                    .forEach(l -> lemmaRepository.updateLemma(l.getId(), l.getFrequency()));
            pageRepository.deleteById(page.getId());
        }
        PageEntity pageEntity;
        pageEntity = createPage(urlPage, siteEntity);
        pageRepository.save(pageEntity);
        return pageEntity;
    }

    public SiteEntity createSite(Site site) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName(site.getName());
        siteEntity.setUrl(site.getUrl());
        siteEntity.setStatus(EnumStatus.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        return siteEntity;
    }

    public PageEntity createPage(String url, SiteEntity siteEntity) throws Exception {
        PageEntity page = new PageEntity();
        page.setPath(url);
        page.setSite(siteEntity);
        Connection connection;
        if (url.startsWith("http")) {
            connection = Jsoup.connect(url);
        } else {
            connection = Jsoup.connect(siteEntity.getUrl().concat(url));
        }
        page.setContent(connection.get().toString());
        int code = connection.response().statusCode();
        if (code != 200) {
            throw new Exception();
        }
        page.setCode(code);
        return page;
    }
}
