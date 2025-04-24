package genum.dataset.service;

import genum.dataset.DTO.CreateDatasetRequest;
import genum.dataset.DTO.DatasetDTO;
import genum.dataset.DTO.DatasetUpdateRequest;
import genum.dataset.domain.*;
import genum.dataset.enums.CollaboratorPermission;
import genum.dataset.enums.DatasetType;
import genum.dataset.enums.PendingActionEnum;
import genum.dataset.enums.Visibility;
import genum.dataset.model.Dataset;
import genum.dataset.repository.DatasetRepository;
import genum.genumUser.repository.GenumUserRepository;
import genum.shared.dataset.exception.DatasetNotFoundException;
import genum.shared.genumUser.exception.BadRequestException;
import genum.shared.genumUser.exception.UserAlreadyExistsException;
import genum.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatasetsServiceImpl {
    private final DatasetRepository datasetsRepository;
    private final DatasetStorageService datasetStorageService;
    private final GenumUserRepository genumUserRepository;
    private final SecurityUtils securityUtils;


    @CacheEvict(value = "dataset_page", allEntries = true)
    public String createDataset(CreateDatasetRequest createNewDatasetDTO, MultipartFile file) throws IOException, IllegalArgumentException {
        try {
            log.info("entered create dataset");
            String currentUserId = securityUtils.getCurrentAuthenticatedUserId();
            var userWithIdFirstnameAndLastname = genumUserRepository
                    .findByCustomUserDetails_UserReferenceIdReturningIdFirstAndName(currentUserId)
                            .stream().findAny().orElseThrow(UserAlreadyExistsException::new);
            log.info("user with id firstname: {}", "userWithIdFirstnameAndLastname");
            DatasetType fileType = validateFileType(file);
            log.info("datasetType: {}", fileType);
            DatasetMetadata metadata = new DatasetMetadata(
                    createNewDatasetDTO.datasetName(),
                    file.getOriginalFilename(),
                    file.getSize(),
                    fileType,
                    Objects.nonNull(
                            createNewDatasetDTO.visibility()) ?
                            Visibility.valueOf(createNewDatasetDTO.visibility().toUpperCase()) : Visibility.PUBLIC
            );
            String uploadUrl = datasetStorageService.storeDataSet(file, metadata);
            log.info("uploadUrl: {}","just testing");
            var dataset = new Dataset();
            dataset.setPendingActions(PendingActions.pendingActions);
            dataset.setDatasetID(UUID.randomUUID().toString());
            dataset.setTags(Set.of());
            dataset.setDatasetType(metadata.getContentType());
            dataset.setVisibility(metadata.getVisibility());
            dataset.setFileName(file.getOriginalFilename());
            dataset.setUploadFileUrl(uploadUrl);
            dataset.setDownloads(0);
            dataset.setUploaderId(currentUserId);
            dataset.setDatasetName(metadata.getDatasetName());
            dataset.setDescription("");
            dataset.setDoiCitation("");
            dataset.setCollaborators(Set.of(new Collaborator("%s %s".formatted(
                    userWithIdFirstnameAndLastname.firstName(),
                    userWithIdFirstnameAndLastname.lastName()),currentUserId, CollaboratorPermission.OWNER)));
            log.info("dataset: {}", dataset);
            return datasetsRepository.save(dataset).getDatasetID();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private DatasetType validateFileType(MultipartFile file) throws IllegalArgumentException {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null ||
                (!originalFileName.toLowerCase().endsWith(".json") &&
                        !originalFileName.toLowerCase().endsWith(".csv"))) {
            throw new IllegalArgumentException("Invalid file type. Only JSON and CSV are allowed.");
        }
        return (originalFileName.endsWith(".json")) ? DatasetType.JSON : DatasetType.CSV;
    }


    @Caching(
            evict = {@CacheEvict(value = "dataset_download_url", key = "#id", beforeInvocation = true)},
            put = {@CachePut(value = "dataset", key = "#id")}
    )
    public DatasetDTO updateDataset(String id, DatasetUpdateRequest updateRequest) {

        return doUpdateDataset(id, updateRequest).toDTO();
    }


    private Dataset doUpdateDataset(String datasetId, DatasetUpdateRequest updateRequest){
        try {
            var existingDataset = getDatasetById(datasetId);
            if (Objects.nonNull(updateRequest.visibility())) {
                existingDataset.setVisibility(Visibility.valueOf(updateRequest.visibility()));
            }

            if (Objects.nonNull(updateRequest.description())) {
                if (existingDataset.getDescription().isBlank()) {
                    existingDataset.setDescription(updateRequest.description());
                    existingDataset.addPendingAction(PendingActionEnum.ADD_DESCRIPTION);
                } else if (!existingDataset.getDescription().equals(updateRequest.description())) {
                    existingDataset.setDescription(updateRequest.description());
                } else {
                    existingDataset.setDescription(updateRequest.description());
                    existingDataset.removePendingAction(PendingActionEnum.ADD_DESCRIPTION);
                }
            }

            if (Objects.nonNull(updateRequest.subtitle())) {
                if (existingDataset.getDatasetSubtitle().isBlank()) {
                    existingDataset.setDatasetSubtitle(updateRequest.subtitle());
                    existingDataset.addPendingAction(PendingActionEnum.ADD_SUBTITLE);
                } else if (!existingDataset.getDatasetSubtitle().equals(updateRequest.subtitle())) {
                    existingDataset.setDatasetSubtitle(updateRequest.subtitle());
                } else {
                    existingDataset.setDatasetSubtitle(updateRequest.subtitle());
                    existingDataset.removePendingAction(PendingActionEnum.ADD_SUBTITLE);
                }
            }

            if (Objects.nonNull(updateRequest.tags())) {
                var tagComparator = new EqualsComparator<Tag>();
                boolean changeInTags = !Arrays.equals(
                        existingDataset.getTags().toArray(Tag[]::new),
                        updateRequest.tags().toArray(Tag[]::new),
                        tagComparator);

                if (existingDataset.getTags().isEmpty()) {
                    existingDataset.setTags(updateRequest.tags());
                    existingDataset.addPendingAction(PendingActionEnum.ADD_TAGS);
                } else if (changeInTags) {
                    existingDataset.addTags(updateRequest.tags());
                } else {
                    existingDataset.setTags(updateRequest.tags());
                    existingDataset.removePendingAction(PendingActionEnum.ADD_TAGS);
                }
            }

            if (Objects.nonNull(updateRequest.collaborators())) {
                var collaboratorComparator = new EqualsComparator<Collaborator>();
                boolean changeInCollaborators = !Arrays.equals(
                        existingDataset.getCollaborators().toArray(Collaborator[]::new),
                        updateRequest.collaborators().toArray(Collaborator[]::new),
                        collaboratorComparator);

                if (changeInCollaborators){
                    existingDataset.setCollaborators(updateRequest.collaborators());
                } else {
                    existingDataset.addCollaborators(updateRequest.collaborators());
                }
            }
            if (Objects.nonNull(updateRequest.authors())) {
                var authorComparator = new EqualsComparator<Author>();
                boolean changeInAuthors = !Arrays.equals(
                        existingDataset.getAuthors().toArray(Author[]::new),
                        updateRequest.authors().toArray(Author[]::new),
                        authorComparator);

                if (changeInAuthors){
                    existingDataset.setAuthors(updateRequest.authors());
                } else {
                    existingDataset.addAuthors(updateRequest.authors());
                }
            }
            if (Objects.nonNull(updateRequest.coverage())) {
                existingDataset.setCoverage(updateRequest.coverage());
            }

            if (Objects.nonNull(updateRequest.doiCitation())) {
                existingDataset.setDoiCitation(updateRequest.doiCitation());
            }

            if (Objects.nonNull(updateRequest.license())) {
                existingDataset.setLicense(updateRequest.license());
            }

            if (Objects.nonNull(updateRequest.provenance())) {
                existingDataset.setProvenance(updateRequest.provenance());
            }

            return datasetsRepository.save(existingDataset);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Cacheable(value = "dataset", key = "#id")

    public DatasetDTO getDatasetDTOById(String id) {
        Dataset dataset = datasetsRepository.getDatasetByDatasetID(id).orElseThrow(() -> new DatasetNotFoundException(id));
        return dataset.toDTO();
    }


    public Dataset getDatasetById(String id) {
        return datasetsRepository.getDatasetByDatasetID(id).orElseThrow(() -> new DatasetNotFoundException(id));
    }


    @Cacheable(value = "dataset_page", keyGenerator = "customPageableKeyGenerator")
    public Page<DatasetDTO> getAllDatasets(Pageable pageable) {
        Page<Dataset> page = datasetsRepository.findAll(pageable);
        return new PageImpl<>(
                page.stream()
                        .map(Dataset::toDTO)
                        .collect(Collectors.toList()),
                page.getPageable(),
                page.getTotalElements());
    }


    @CacheEvict(value = "dataset", key = "#id")
    public void deleteDataset(String id) throws DatasetNotFoundException {
        if (datasetsRepository.existsByDatasetID(id)) {
            datasetsRepository.deleteByDatasetID(id);
        } else {
            throw new DatasetNotFoundException("This dataset is not found or has been deleted");
        }
    }


    @Cacheable(value = "trending_dataset_page", keyGenerator = "customPageableKeyGenerator")
    public Page<DatasetDTO> trending(Pageable pageable) {
        List<DatasetDTO> datasets = datasetsRepository.findTop100ByOrderByDownloadsDesc().stream()
                .map(Dataset::toDTO)
                .collect(Collectors.toList());
        int start = Math.min((int) pageable.getOffset(), datasets.size());
        int end = Math.min((start + pageable.getPageSize()), datasets.size());
        return new PageImpl<>(datasets.subList(start, end), pageable, datasets.size());

    }


    @Caching(
            cacheable = @Cacheable(value = "dataset_download_url", key = "#id"),
            put = @CachePut(value = "dataset", key = "#id")
    )
    public String downloadDataset(String id) {
        var dataset = incrementDownloadCount(id);
        return dataset.getUploadFileUrl();

    }
    @Cacheable(value = "dataset_tags")
    public Set<Tag> getAllTags() {
        return DatasetTags.getTags();
    }
    @Cacheable(value = "dataset_licenses")
    public Set<License> getAllLicences() {
        return Licenses.getLicenses();
    }


    public DatasetDTO upvoteDataset(String id) {
        Dataset dataset = getDatasetById(id);
        dataset.addUsersThatLiked(securityUtils.getCurrentAuthenticatedUserId());
        return datasetsRepository.save(dataset).toDTO();
    }


    private Dataset incrementDownloadCount(String id) {
        Dataset dataset = getDatasetById(id);
        dataset.setDownloads(dataset.getDownloads() + 1);
        datasetsRepository.save(dataset);
        return dataset;
    }

}