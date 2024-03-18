package searchengine.dto.response;

import lombok.Data;

@Data
public class Message {
    private boolean result;
    private String error;

    public Message(boolean result) {
        this.result = result;
    }

    public Message(boolean result, String error) {
        this(result);
        this.error = error;
    }
}
