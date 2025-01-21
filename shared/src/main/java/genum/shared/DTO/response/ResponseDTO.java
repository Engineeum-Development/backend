package genum.shared.data.data.DTO.response;

import genum.shared.data.genumUser.GenumUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {

    private GenumUser genumUser;
    private String token;
}
