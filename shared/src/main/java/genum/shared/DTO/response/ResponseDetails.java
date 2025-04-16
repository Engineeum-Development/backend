package genum.shared.DTO.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ResponseDetails<T> {
    private String timestamp;
    private String message;
    private String status;
    private T data;

    public ResponseDetails(LocalDateTime timestamp, String message, String status) {
        this.timestamp = timestamp.toString();
        this.message = message;
        this.status = status;
    }
    public ResponseDetails(LocalDateTime timestamp, String message, String status, T data) {
        this.timestamp = timestamp.toString();
        this.message = message;
        this.status = status;
        this.data = data;
    }
    public ResponseDetails(String message, String status) {
        this.timestamp = LocalDateTime.now().toString();
        this.message = message;
        this.status = status;
    }
    public ResponseDetails(String message, String status, T data) {
        this.timestamp = LocalDateTime.now().toString();
        this.message = message;
        this.status = status;
        this.data = data;
    }
}
