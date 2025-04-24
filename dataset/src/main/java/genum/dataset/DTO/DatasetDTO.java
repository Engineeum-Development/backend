package genum.dataset.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import genum.dataset.domain.*;

import java.io.Serializable;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DatasetDTO(
        String datasetId,
        String name,
        String subtitle,
        String description,
        String thumbnailURL,
        String fileDownloadUrl,
        Set<Tag> tags,
        Set<PendingAction> pendingActions,
        String datasetFormat,
        long downloads,
        License license,
        String doiCitation,
        Provenance provenance,
        Set<Collaborator> collaborators,
        long upvotes,
        Set<Author> authors,
        Coverage coverage


) implements Serializable {
}
