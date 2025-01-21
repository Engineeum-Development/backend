package genum.shared.DTO.response;

import genum.shared.genumUser.GenumUserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {

    private GenumUserDTO genumUser;
    private String token;
}
