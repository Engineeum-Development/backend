package genum.dataset.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record Author(String authorName, String authorBio) implements Serializable {
}
