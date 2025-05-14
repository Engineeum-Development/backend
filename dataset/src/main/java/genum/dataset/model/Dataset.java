package genum.dataset.model;

import genum.dataset.DTO.DatasetDTO;
import genum.dataset.domain.*;
import genum.dataset.enums.DatasetType;
import genum.dataset.enums.PendingActionEnum;
import genum.dataset.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
@Data
@Builder
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
    private String filePublicId;
    private Visibility visibility;
    private Set<Tag> tags;
    private Set<PendingAction> pendingActions;
    private DatasetType datasetType;
    private String fileName;

    private long fileSize;
    private AtomicInteger downloads;

    private License license;
    private String doiCitation;
    private Provenance provenance;
    private Set<Author> authors;
    private Coverage coverage;

    private Set<Collaborator> collaborators;

    private Set<String> usersThatUpvote;

    {
        tags = new HashSet<>();
        pendingActions = new HashSet<>();
        collaborators = new HashSet<>();
        usersThatUpvote = new HashSet<>();
        downloads = new AtomicInteger(0);

    }

    public void addUsersThatLiked(String userId) {
        usersThatUpvote.add(userId);
    }
    public void addTags(Set<Tag> newTags) {
        this.tags.addAll(Objects.requireNonNull(newTags, "tags added can't be null"));
    }
    public void addCollaborators(Set<Collaborator> collaborators) {
        this.collaborators.addAll(Objects.requireNonNull(collaborators, "Collaborators can't be null"));
    }
    public void addAuthors(Set<Author> author) {
        this.authors.addAll(Objects.requireNonNull(author,"Authors can't be null"));
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
        if (Objects.isNull(usersThatUpvote)) setUsersThatUpvote(new HashSet<>());
        if (Objects.isNull(tags)) setTags(new HashSet<>());
        if (Objects.isNull(pendingActions)) setTags(new HashSet<>());
        if (Objects.isNull(collaborators)) setTags(new HashSet<>());
        if (Objects.isNull(authors)) setAuthors(new HashSet<>());
        if (Objects.isNull(downloads)) setDownloads(new AtomicInteger());
        return new DatasetDTO(
                this.getDatasetID(),
                this.getDatasetName(),
                this.getDatasetSubtitle(),
                this.getDescription(),
                this.getDatasetThumbnailImageUrl(),
                this.getUploadFileUrl(),
                this.getTags(),
                this.getPendingActions(),
                this.getDatasetType().toString(),
                this.getDownloads().get(),
                this.getLicense(),
                this.getDoiCitation(),
                this.getProvenance(),
                this.getCollaborators(),
                this.getUsersThatUpvote().size(),
                this.getAuthors(),
                this.getCoverage()

        );
    }
}
