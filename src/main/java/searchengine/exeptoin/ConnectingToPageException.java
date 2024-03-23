package searchengine.exeptoin;

import searchengine.model.PageEntity;

public class ConnectingToPageException extends Exception {
    public ConnectingToPageException(String message, PageEntity pageEntity, int code) {
        super(message);
        pageEntity.setCode(code);
        pageEntity.setContent(message);
    }
}
