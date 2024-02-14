package searchengine.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ResponseMessage {
    private boolean result;
    private String error;

    public ResponseMessage(boolean result) {
        this.result = result;
    }

    public ResponseMessage(boolean result, String error) {
        this(result);
        this.error = error;
    }
}
