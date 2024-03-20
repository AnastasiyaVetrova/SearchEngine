package searchengine.regex;

import lombok.Getter;
import searchengine.model.SiteEntity;

public class BaseRegex {
    @Getter
    private static final String REGEX_URL ="https?://(www.)?";
    @Getter
    private static final String REGEX_WORD = "[^А-яЁё]+";
    public static String getBaseUrl(SiteEntity siteEntity){
        String res=siteEntity.getUrl().replaceAll(REGEX_URL,"");
        String baseUrlRegex="http[s]?://(www.)?("+res+")";
        return baseUrlRegex;
    }
}
