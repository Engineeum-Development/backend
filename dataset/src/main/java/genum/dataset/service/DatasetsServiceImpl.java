package genum.dataset.service;

import genum.dataset.DTO.CreateDatasetDTO;
import genum.dataset.domain.DatasetDownloadData;
import genum.dataset.domain.DatasetMetadata;
import genum.dataset.domain.DatasetType;
import genum.dataset.model.Dataset;
import genum.dataset.repository.DatasetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatasetsServiceImpl implements DatasetsService {
    private final ModelMapper modelMapper = new ModelMapper();

    private final DatasetRepository datasetsRepository;
    private final DatasetStorageService datasetStorageService;


    @Override
    public String createDataset(CreateDatasetDTO createNewDatasetDTO, MultipartFile file) {
        DatasetType fileType = validateFileType(file);
        DatasetMetadata metadata = new DatasetMetadata(
                createNewDatasetDTO.getDescription(),
                createNewDatasetDTO.getTags(),
                file.getOriginalFilename(),
                file.getSize(),
                fileType,
                createNewDatasetDTO.getVisibility()
        );
        String uploadUrl = datasetStorageService.storeDataSet(file, metadata);

        var dataset = new Dataset();
        dataset.setDatasetID(UUID.randomUUID().toString());
        dataset.setTags(metadata.getTags());
        dataset.setDatasetType(metadata.getContentType());
        dataset.setVisibility(createNewDatasetDTO.getVisibility());
        dataset.setTitle(file.getOriginalFilename());
        dataset.setUploadFileUrl(uploadUrl);
        dataset.setDownloads(0);
            return datasetsRepository.save(dataset).getDatasetID();

//        if (dataset.getTitle() == null || dataset.getTitle().length() < 6 || dataset.getTitle().length() > 50) {
//            throw new IllegalArgumentException("Title must be between 6 and 50 characters.");
//        }
//        if (dataset.getUploadFileUrl() == null || dataset.getUploadFileUrl().isEmpty()) {
//            throw new IllegalArgumentException("File URL must be provided.");
//        }
//        if (dataset.getVisibility() == null) {
//            throw new IllegalArgumentException("Visibility must be selected.");
//        }

//        File file = new File(dataset.getUploadFileUrl());
//        if (file.exists() && file.isFile()) {
//            dataset.setFileSize(String.format("%.2f MB", file.length() / (1024.0 * 1024.0)));
//        } else {
//            throw new IllegalArgumentException("File does not exist at the provided URL.");
//        }


    }
    private DatasetType validateFileType(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null ||
                (!originalFileName.toLowerCase().endsWith(".json") &&
                        !originalFileName.toLowerCase().endsWith(".csv"))){
            throw new IllegalArgumentException("Invalid file type. Only JSON and CSV are allowed.");
        }
        return (originalFileName.endsWith(".json")) ? DatasetType.JSON : DatasetType.CSV;
    }

    @Override
    public DatasetMetadata updateDataset(String id, DatasetMetadata metadata) {
        DatasetMetadata existingDataset = getDatasetMetadataById(id);
        Dataset updatedDataSet =  new Dataset();
        updatedDataSet.setDatasetID(id);
        updatedDataSet.setDatasetType(metadata.getContentType());
        updatedDataSet.setVisibility(metadata.getVisibility());
        updatedDataSet.setTags(metadata.getTags());
        updatedDataSet.setDescription(metadata.getDescription());
        updatedDataSet.setTitle(metadata.getOriginalFilename());
//        if (metadata.getTitle() != null && metadata.getTitle().length() >= 6 && metadata.getTitle().length() <= 50) {
//            existingDataset.setTitle(metadata.getTitle());
//        } else {
//            throw new IllegalArgumentException("Title must be between 6 and 50 characters.");
//        }
//        if (metadata.getUploadFile() != null && !metadata.getUploadFile().isEmpty()) {
//            existingDataset.setUploadFile(metadata.getUploadFile());
//            File file = new File(metadata.getUploadFile());
//            if (file.exists() && file.isFile()) {
//                existingDataset.setFileSize(String.format("%.2f MB", file.length() / (1024.0 * 1024.0)));
//            } else {
//                throw new IllegalArgumentException("File does not exist at the provided URL.");
//            }
//        }
//        if (metadata.getVisibility() != null) {
//            existingDataset.setVisibility(metadata.getVisibility());
//        } else {
//            throw new IllegalArgumentException("Visibility must be selected.");
//        }
        return ((Dataset)datasetsRepository.save(updatedDataSet)).toMetadata();
    }

    @Override
    public DatasetMetadata getDatasetMetadataById(String id) {
        Optional<Dataset> dataset = datasetsRepository.getDatasetByDatasetID(id);
        return dataset.map(dataset1 -> new DatasetMetadata(
                dataset1.getDatasetID(),
                dataset1.getDescription(),
                dataset1.getTags(),
                dataset1.getTitle(),
                dataset1.getFileSize(),
                dataset1.getDatasetType(),
                dataset1.getVisibility()
        )).orElseThrow(() -> new RuntimeException("Dataset not found with ID: " + id));
    }

    @Override
    public Dataset getDatasetById(String id) {
        return datasetsRepository.getDatasetByDatasetID(id).orElseThrow(() -> new RuntimeException("Dataset not found with ID: " + id));
    }

    @Override
    public Page<DatasetMetadata> getAllDatasets(Pageable pageable) {
        Page<Dataset> page =  datasetsRepository.findAll(pageable);
        return new PageImpl<>(
                page.stream()
                        .map(dataset ->
                                new DatasetMetadata(
                                        dataset.getDatasetID(),
                                        dataset.getDescription(),
                                        dataset.getTags(),
                                        dataset.getTitle(),
                                        dataset.getFileSize(),
                                        dataset.getDatasetType(),
                                        dataset.getVisibility()
                                )).collect(Collectors.toList()), page.getPageable(), page.getTotalElements());
    }

    @Override
    public void deleteDataset(String id) {
        if (datasetsRepository.existsByDatasetID(id)) {
            datasetsRepository.deleteByDatasetID(id);
        }
    }

    @Override
    public List<DatasetMetadata> trending() {
        List<Dataset> datasets = datasetsRepository.findAll();
        return datasets.stream()
                .sorted((d1, d2) -> Integer.compare(d2.getDownloads(), d1.getDownloads()))
                .map(Dataset::toMetadata)
                .collect(Collectors.toList());
    }

    @Override
    public DatasetDownloadData downloadDataset(String id) {
        incrementDownloadCount(id);
        Dataset dataset = getDatasetById(id);
        DatasetMetadata metadata = dataset.toMetadata();
        var multipartFile = datasetStorageService.getDataSet(dataset.getUploadFileUrl());
        return new DatasetDownloadData(metadata, multipartFile);

}

    private void incrementDownloadCount(String id) {
        Dataset dataset = getDatasetById(id);
        dataset.setDownloads(dataset.getDownloads() + 1);
        datasetsRepository.save(dataset);
    }
}