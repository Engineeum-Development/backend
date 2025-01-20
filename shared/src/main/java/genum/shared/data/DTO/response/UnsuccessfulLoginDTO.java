package genum.shared.data.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//@Data
//@AllArgsConstructor
//@NoArgsConstructor
public class UnsuccessfulLoginDTO {

    public UnsuccessfulLoginDTO() {
    }

    public UnsuccessfulLoginDTO(LocalDateTime timestamp, String message, String requestType, String path) {
        this.timestamp = timestamp;
        this.message = message;
        this.requestType = requestType;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private LocalDateTime timestamp;

    private String message;

    private String requestType;

    private String path;

}
