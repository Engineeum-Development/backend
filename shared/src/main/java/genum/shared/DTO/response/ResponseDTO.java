package genum.shared.DTO.response;


import genum.shared.genumUser.GenumUserDTO;

public class ResponseDTO {

    private GenumUserDTO genumUser;
    private String token;

    public ResponseDTO() {
    }

    public ResponseDTO(GenumUserDTO genumUser, String token) {
        this.genumUser = genumUser;
        this.token = token;
    }

    public GenumUserDTO getGenumUser() {
        return genumUser;
    }

    public void setGenumUser(GenumUserDTO genumUser) {
        this.genumUser = genumUser;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
