package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
//класс бин, который берет сайты из app по форме класса Site
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class SitesList {
    private List<Site> sites;
}
