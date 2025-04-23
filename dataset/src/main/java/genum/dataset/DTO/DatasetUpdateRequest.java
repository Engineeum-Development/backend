package genum.dataset.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import genum.dataset.domain.Collaborator;
import genum.dataset.domain.License;
import genum.dataset.domain.Provenance;
import genum.dataset.domain.Tag;


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
        Set<Tag> tags
) implements Serializable {
}
