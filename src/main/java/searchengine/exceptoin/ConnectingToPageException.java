package searchengine.exceptoin;

import searchengine.Application;
import searchengine.model.PageEntity;

public class ConnectingToPageException extends Exception {
    public ConnectingToPageException(String message, PageEntity pageEntity, int code) {
        super(message);
        pageEntity.setCode(code);
        pageEntity.setContent(message);
        Application.LOG.error("Ошибка обработана: ConnectingToPageException \n:"+message);
    }
}
