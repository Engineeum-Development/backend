package genum.dataset.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record PendingAction(String name, String description) implements Serializable {
}
