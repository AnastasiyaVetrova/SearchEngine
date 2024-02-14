//package searchengine.parsers;
//
//import searchengine.model.PageEntity;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.TreeSet;
//import java.util.concurrent.CopyOnWriteArraySet;
//
//public class SiteMap1 {
//
//    private PageEntity pageEntity;
//
//    private static final CopyOnWriteArraySet<PageEntity> WORK_PAGE = new CopyOnWriteArraySet<>();//содержит все ссылки
//
//    public SiteMap1(PageEntity pageEntity) {
//        this.pageEntity = pageEntity;
//
//    }
//
//    public CopyOnWriteArraySet<PageEntity> compute() {
//        CopyOnWriteArraySet<PageEntity> resultUrl = new CopyOnWriteArraySet<>();//результат
//        ParseHTML parseHTML = new ParseHTML();//парсинг- pageEntity
////        CopyOnWriteArraySet<PageEntity> allUrl = new CopyOnWriteArraySet<>();//получение pageEntity
//        TreeSet<PageEntity> allUrl = parseHTML.getParseUrl(pageEntity);
//        List<SiteMap1> siteLink = new ArrayList<>();//лист с заданиями - url
//
//        try {
////            TreeSet treeSet = parseHTML.getParseUrl(pageEntity);
////            allUrl.addAll(parseHTML.getParseUrl(pageEntity));//получение pageEntity
////            allUrl.addAll(treeSet);
//            Thread.sleep(50);
//        } catch (Exception exception) {
//            exception.printStackTrace();
//        }
//        if (!allUrl.isEmpty()) {
//            for (PageEntity page : allUrl) {
//                if (WORK_PAGE.contains(page)) {
//                    continue;
//                }
//                resultUrl.add(page);
//                WORK_PAGE.add(page);
//                SiteMap1 siteMap = new SiteMap1(page);
//                siteLink.add(siteMap);
//            }
//        } else {
//            resultUrl.add(pageEntity);
//        }
//        return resultUrl;
//    }
//}

