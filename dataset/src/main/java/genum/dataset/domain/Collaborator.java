package genum.dataset.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import genum.dataset.enums.CollaboratorPermission;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record Collaborator(String collaboratorName,String collaboratorId, CollaboratorPermission collaboratorPermission) implements Serializable {
}
