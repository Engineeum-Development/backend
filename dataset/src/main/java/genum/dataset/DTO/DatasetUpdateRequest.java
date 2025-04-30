package genum.dataset.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import genum.dataset.domain.*;


import java.io.Serializable;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public record DatasetUpdateRequest (
        Set<Collaborator> collaborators,
        License license,
        String doiCitation,
        String subtitle,
        Provenance provenance,
        String description,
        String visibility,
        Set<Tag> tags,
        Set<Author> authors,
        Coverage coverage
) implements Serializable {
}
