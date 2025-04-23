package genum.dataset.model;

import genum.dataset.DTO.DatasetDTO;
import genum.dataset.domain.*;
import genum.dataset.enums.PendingActionEnum;
import genum.dataset.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Dataset implements Serializable {

    @Id
    private String id;
    private String datasetID;
    private String datasetName;
    private String datasetSubtitle;
    private String description;
    private String uploaderId;
    private String datasetThumbnailImageUrl;
    private String uploadFileUrl;
    private Visibility visibility;
    private Set<Tag> tags;
    private Set<PendingAction> pendingActions;
    private DatasetType datasetType;
    private String fileName;

    private long fileSize;
    private int downloads;

    private License license;
    private String doiCitation;
    private Provenance provenance;
    private String authorName;

    private Set<Collaborator> collaborators;

    private Set<String> usersThatUpvote;

    {
        usersThatUpvote = new HashSet<>();
    }

    public void addUsersThatLiked(String userId) {
        usersThatUpvote.add(userId);
    }
    public void addTags(Set<Tag> newTags) {
        this.tags.addAll(Objects.requireNonNull(newTags, "tags added can't be null"));
    }
    public void addCollaborator(Collaborator collaborator) {
        this.collaborators.add(Objects.requireNonNull(collaborator, "Collaborator id can't be null"));
    }
    public void addCollaborators(Set<Collaborator> collaborators) {
        this.collaborators.addAll(Objects.requireNonNull(collaborators, "Collaborators id can't be null"));
    }
    public void removePendingAction(PendingActionEnum pendingActionEnum) {
        pendingActions.remove(
                PendingActions.pendingActions.stream()
                        .filter(pendingAction -> pendingActionEnum.getName().equals(pendingAction.name()))
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid pending action")));
    }
    public void addPendingAction(PendingActionEnum pendingActionEnum) {
        pendingActions.add(
                PendingActions.pendingActions.stream()
                        .filter(pendingAction -> pendingActionEnum.getName().equals(pendingAction.name()))
                        .findAny().orElseThrow(() -> new IllegalArgumentException("Invalid pending action"))
        );
    }


    public DatasetDTO toDTO() {
        return new DatasetDTO(
                this.getDatasetID(),
                this.getDatasetName(),
                this.getDatasetSubtitle(),
                this.getDescription(),
                this.getAuthorName(),
                this.getDatasetThumbnailImageUrl(),
                this.getUploadFileUrl(),
                this.getTags(),
                this.getPendingActions(),
                this.getDatasetType().toString(),
                this.getDownloads(),
                this.getLicense(),
                this.getDoiCitation(),
                this.getProvenance(),
                this.getCollaborators(),
                this.getUsersThatUpvote().size()

        );
    }
}
