package genum.dataset.service;

import genum.dataset.DTO.CreateDatasetDTO;
import genum.dataset.model.Datasets;
import genum.dataset.repository.DatasetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatasetsServiceImpl implements DatasetsService {
    private final ModelMapper modelMapper = new ModelMapper();

    private final DatasetRepository datasetsRepository;


    @Override
    public Datasets createDataset(CreateDatasetDTO createNewDatasetDTO) {
        Datasets datasets = new Datasets();
        modelMapper.map(createNewDatasetDTO, datasets);

        if (datasets.getTitle() == null || datasets.getTitle().length() < 6 || datasets.getTitle().length() > 50) {
            throw new IllegalArgumentException("Title must be between 6 and 50 characters.");
        }
        if (datasets.getUploadFile() == null || datasets.getUploadFile().isEmpty()) {
            throw new IllegalArgumentException("File URL must be provided.");
        }
        if (datasets.getVisibility() == null) {
            throw new IllegalArgumentException("Visibility must be selected.");
        }

        File file = new File(datasets.getUploadFile());
        if (file.exists() && file.isFile()) {
            datasets.setFileSize(String.format("%.2f MB", file.length() / (1024.0 * 1024.0)));
        } else {
            throw new IllegalArgumentException("File does not exist at the provided URL.");
        }

        datasets.setVisibility(createNewDatasetDTO.getVisibility());
        datasets.setTitle(createNewDatasetDTO.getTitle());
        datasets.setUploadFile(createNewDatasetDTO.getUploadFile());
        datasets.setDownloads(0);
        return datasetsRepository.save(datasets);
    }

    @Override
    public Datasets updateDataset(String id, Datasets updatedDataset) {
        Datasets existingDataset = getDatasetById(id);
        if (updatedDataset.getTitle() != null && updatedDataset.getTitle().length() >= 6 && updatedDataset.getTitle().length() <= 50) {
            existingDataset.setTitle(updatedDataset.getTitle());
        } else {
            throw new IllegalArgumentException("Title must be between 6 and 50 characters.");
        }
        if (updatedDataset.getUploadFile() != null && !updatedDataset.getUploadFile().isEmpty()) {
            existingDataset.setUploadFile(updatedDataset.getUploadFile());
            File file = new File(updatedDataset.getUploadFile());
            if (file.exists() && file.isFile()) {
                existingDataset.setFileSize(String.format("%.2f MB", file.length() / (1024.0 * 1024.0)));
            } else {
                throw new IllegalArgumentException("File does not exist at the provided URL.");
            }
        }
        if (updatedDataset.getVisibility() != null) {
            existingDataset.setVisibility(updatedDataset.getVisibility());
        } else {
            throw new IllegalArgumentException("Visibility must be selected.");
        }
        return datasetsRepository.save(existingDataset);
    }

    @Override
    public Datasets getDatasetById(String id) {
        Optional<Datasets> dataset = datasetsRepository.findById(id);
        return dataset.orElseThrow(() -> new RuntimeException("Dataset not found with ID: " + id));
    }

    @Override
    public List<Datasets> getAllDatasets() {
        return datasetsRepository.findAll();
    }

    @Override
    public void deleteDataset(String id) {
        Datasets dataset = getDatasetById(id);
        datasetsRepository.delete(dataset);
    }

    @Override
    public List<Datasets> trending() {
        List<Datasets> datasets = datasetsRepository.findAll();
        return datasets.stream()
                .sorted((d1, d2) -> Integer.compare(d2.getDownloads(), d1.getDownloads()))
                .collect(Collectors.toList());
    }

    @Override
    public void downloadDataset(String id) {
        incrementDownloadCount(id);
        Datasets dataset = getDatasetById(id);
        File file = new File(dataset.getUploadFile());
        if (!file.exists()) {
            throw new RuntimeException("File not found for download.");
        }
        log.info("Downloading file: {}", file.getAbsolutePath());
}

    private void incrementDownloadCount(String id) {
        Datasets dataset = getDatasetById(id);
        dataset.setDownloads(dataset.getDownloads() + 1);
        datasetsRepository.save(dataset);
    }
}