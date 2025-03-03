package genum.dataset.service;

import genum.dataset.DTO.CreateDatasetDTO;
import genum.dataset.domain.DatasetMetadata;
import genum.dataset.domain.DatasetType;
import genum.dataset.model.Dataset;
import genum.dataset.repository.DatasetRepository;
import genum.genumUser.repository.GenumUserRepository;
import genum.shared.dataset.exception.DatasetNotFoundException;
import genum.shared.genumUser.exception.GenumUserNotFoundException;
import genum.shared.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatasetsServiceImpl implements DatasetsService {
    private final DatasetRepository datasetsRepository;
    private final DatasetStorageService datasetStorageService;
    private final GenumUserRepository genumUserRepository;


    @Override
    @CacheEvict(value = "dataset_metadata_page", allEntries = true)
    public String createDataset(CreateDatasetDTO createNewDatasetDTO, MultipartFile file) throws IOException, IllegalArgumentException {
        log.info("entered create dataset");
        var userCredentials = getAuthenticatedUserCredentials();
        var user = genumUserRepository.findByCustomUserDetailsEmail(userCredentials.getEmail()).orElseThrow(GenumUserNotFoundException::new);
        DatasetType fileType = validateFileType(file);
        log.info("datasetType: {}", fileType);
        DatasetMetadata metadata = new DatasetMetadata(
                createNewDatasetDTO.getDescription(),
                createNewDatasetDTO.getTags(),
                file.getOriginalFilename(),
                file.getSize(),
                fileType,
                createNewDatasetDTO.getVisibility()
        );
        log.info("metadata: {}", metadata);
        String uploadUrl = datasetStorageService.storeDataSet(file, metadata);
        log.info("uploadUrl: {}", uploadUrl);
        var dataset = new Dataset();
        dataset.setDatasetID(UUID.randomUUID().toString());
        dataset.setTags(metadata.getTags());
        dataset.setDatasetType(metadata.getContentType());
        dataset.setVisibility(createNewDatasetDTO.getVisibility());
        dataset.setTitle(file.getOriginalFilename());
        dataset.setUploadFileUrl(uploadUrl);
        dataset.setDownloads(0);
        dataset.setUploader(user.getId());
        log.info("dataset: {}", dataset);
        return datasetsRepository.save(dataset).getDatasetID();
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

    @Override
    @Caching(
            evict = {@CacheEvict(value = "dataset_download_url", key = "#id", beforeInvocation = true)},
            put = {@CachePut(value = "dataset_metadata", key = "#id")}
    )
    public DatasetMetadata updateDatasetMetadata(String id, DatasetMetadata metadata) {

        return updateDataset(id, metadata).toMetadata();
    }

    @Override
    public Dataset updateDataset(String id, DatasetMetadata metadata) {
        var existingDataset = getDatasetById(id);
        var updatedDataSet = new Dataset();
        updatedDataSet.setId(existingDataset.getId());
        updatedDataSet.setDatasetID(id);
        updatedDataSet.setDatasetType(metadata.getContentType());
        updatedDataSet.setVisibility(metadata.getVisibility());
        updatedDataSet.setTags(metadata.getTags());
        updatedDataSet.setDescription(metadata.getDescription());
        updatedDataSet.setTitle(metadata.getOriginalFilename());

        return datasetsRepository.save(updatedDataSet);
    }

    @Cacheable(value = "dataset_metadata", key = "#id")
    @Override
    public DatasetMetadata getDatasetMetadataById(String id) {
        Optional<Dataset> dataset = datasetsRepository.getDatasetByDatasetID(id);

        return dataset.map(Dataset::toMetadata).orElseThrow(() -> new RuntimeException("Dataset not found with ID: " + id));
    }

    @Override
    public Dataset getDatasetById(String id) {
        return datasetsRepository.getDatasetByDatasetID(id).orElseThrow(() -> new DatasetNotFoundException(id));
    }

    @Override
    @Cacheable(value = "dataset_metadata_page", keyGenerator = "customPageableKeyGenerator")
    public Page<DatasetMetadata> getAllDatasets(Pageable pageable) {
        Page<Dataset> page = datasetsRepository.findAll(pageable);
        return new PageImpl<>(
                page.stream()
                        .map(Dataset::toMetadata)
                        .collect(Collectors.toList()),
                page.getPageable(),
                page.getTotalElements());
    }

    @Override
    @CacheEvict(value = "dataset_metadata", key = "#id")
    public void deleteDataset(String id) {
        datasetsRepository.deleteByDatasetID(id);

    }

    @Override
    @Cacheable(value = "trending_dataset_metadata_page", keyGenerator = "customPageableKeyGenerator")
    public Page<DatasetMetadata> trending(Pageable pageable) {
        List<DatasetMetadata> datasets = datasetsRepository.findTop100ByOrderByDownloadsDesc().stream()
                .map(Dataset::toMetadata)
                .collect(Collectors.toList());
        int start = Math.min((int) pageable.getOffset(), datasets.size());
        int end = Math.min((start + pageable.getPageSize()), datasets.size());
        return new PageImpl<>(datasets.subList(start, end), pageable, datasets.size());

    }

    @Override
    @Caching(
            cacheable = @Cacheable(value = "dataset_download_url", key = "#id"),
            put = @CachePut(value = "dataset_metadata", key = "#id")
    )
    public String downloadDataset(String id) {
        var dataset = incrementDownloadCount(id);
        return dataset.getUploadFileUrl();

    }

    @Override
    public void likeDataset(String id) {
        var userCredentials = getAuthenticatedUserCredentials();
        String userId = genumUserRepository.findUserIdByEmail(userCredentials.getEmail());
        Dataset dataset = getDatasetById(id);
        dataset.addUsersThatLiked(userId);
        datasetsRepository.save(dataset);
    }


    private Dataset incrementDownloadCount(String id) {
        Dataset dataset = getDatasetById(id);
        dataset.setDownloads(dataset.getDownloads() + 1);
        datasetsRepository.save(dataset);
        return dataset;
    }

    private CustomUserDetails getAuthenticatedUserCredentials() {
        return (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}