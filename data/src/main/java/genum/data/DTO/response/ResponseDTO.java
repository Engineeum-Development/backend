package genum.data.DTO.response;

import genum.data.genumUser.GenumUser;
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
