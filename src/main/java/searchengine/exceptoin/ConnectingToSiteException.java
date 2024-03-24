package searchengine.exceptoin;

import searchengine.Application;
import searchengine.model.EnumStatus;
import searchengine.model.SiteEntity;
import searchengine.repositories.SiteRepository;

public class ConnectingToSiteException extends Exception {

    public ConnectingToSiteException(String message, SiteRepository siteRepository, SiteEntity siteEntity) {
        super(message);
        siteEntity.setError(message);
        siteEntity.setStatus(EnumStatus.FAILED);
        siteRepository.save(siteEntity);
        Application.LOG.error("Ошибка обработана: ConnectingToSiteException: \n" + message);
    }
}
