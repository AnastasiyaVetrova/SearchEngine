package searchengine.parsers;

import lombok.Getter;
import searchengine.model.SiteEntity;

public class BaseUrlRegex {
    @Getter
    private static final String regex="https?://(www.)?";
    protected static String getBaseUrl(SiteEntity siteEntity){

        String res=siteEntity.getUrl().replaceAll(regex,"");
//        String regex1 = "https?://(www.)?"+res+"[#^\\s]+";
        String baseUrlRegex="http[s]?://(www.)?("+res+")";

        return baseUrlRegex;
    }
}
