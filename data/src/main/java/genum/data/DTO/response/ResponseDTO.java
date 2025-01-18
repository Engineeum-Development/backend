package genum.data.DTO.response;

import genumUser.GenumUser;

//@Data
//@AllArgsConstructor
//@NoArgsConstructor
public class ResponseDTO {

    private GenumUser genumUser;

    private String token;

    public ResponseDTO() {
    }

    public ResponseDTO(GenumUser genumUser, String token) {
        this.genumUser = genumUser;
        this.token = token;
    }

    public GenumUser getGenumUser() {
        return genumUser;
    }

    public void setGenumUser(GenumUser genumUser) {
        this.genumUser = genumUser;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
