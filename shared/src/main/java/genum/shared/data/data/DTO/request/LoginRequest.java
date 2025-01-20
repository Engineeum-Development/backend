package genum.shared.data.data.DTO.request;


import org.springframework.util.Assert;

import java.util.Objects;

public record LoginRequest (String email, String password
){

    public LoginRequest {
        assert Objects.nonNull(email) && Objects.nonNull(password);
    }

}
