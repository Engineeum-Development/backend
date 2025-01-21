package genum.shared.data.data.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnsuccessfulLoginDTO {

    private LocalDateTime timestamp;

    private String message;

    private String requestType;

    private String path;

}
