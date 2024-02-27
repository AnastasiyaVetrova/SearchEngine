package searchengine.parsers;

import searchengine.model.SiteEntity;

public class BaseUrlRegex {
    protected static String getBaseUrl(SiteEntity siteEntity){
        String regex="https?://(www.)?";
        String res=siteEntity.getUrl().replaceAll(regex,"");
//        String regex1 = "https?://(www.)?"+res+"[#^\\s]+";
        String baseUrlRegex="http[s]?://(www.)?("+res+")";

        return baseUrlRegex;
    }
}
