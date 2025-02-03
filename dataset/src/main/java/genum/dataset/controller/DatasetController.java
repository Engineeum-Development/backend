package genum.dataset.controller;
import genum.dataset.DTO.CreateDatasetDTO;
import genum.dataset.domain.DatasetDownloadData;
import genum.dataset.domain.DatasetMetadata;
import genum.dataset.model.Dataset;
import genum.dataset.service.DatasetsServiceImpl;
import genum.shared.DTO.response.ResponseDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/dataset")
@Slf4j
@RequiredArgsConstructor
public class DatasetController {


    private final DatasetsServiceImpl datasetsService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createDataset(@Valid @RequestPart("metadata") CreateDatasetDTO createDatasetDTO,
                                           @RequestPart("file")MultipartFile file) {
        try {
            var createdDataset = datasetsService.createDataset(createDatasetDTO, file);
            var responseDetails = new ResponseDetails<>(
                    LocalDateTime.now(),
                    "Dataset created successfully.",
                    HttpStatus.CREATED.toString(),
                    Map.of("datasetUrl", createdDataset)
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDetails);
        } catch (Exception e) {
            var responseDetails = new ResponseDetails<>(
                    LocalDateTime.now(),
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.toString()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDetails);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDataset(@PathVariable String id, @Valid @RequestBody DatasetMetadata updatedDataset) {
        try {
            DatasetMetadata updated = datasetsService.updateDataset(id, updatedDataset);
            var responseDetails = new ResponseDetails<>(
                    LocalDateTime.now(),
                    "Dataset updated successfully.",
                    HttpStatus.OK.toString()
            );
            return ResponseEntity.ok(responseDetails);
        } catch (Exception e) {
            var responseDetails = new ResponseDetails<>(
                    LocalDateTime.now(),
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.toString()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDetails);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDatasetById(@PathVariable String id) {
        try {
            DatasetMetadata dataset = datasetsService.getDatasetMetadataById(id);
            ResponseDetails<DatasetMetadata> responseDetails = new ResponseDetails<>(
                    LocalDateTime.now(),
                    "Dataset found",
                    HttpStatus.OK.toString(),
                    dataset
            );
            return ResponseEntity.ok(dataset);
        } catch (RuntimeException e) {
            var responseDetails = new ResponseDetails<>(
                    LocalDateTime.now(),
                    e.getMessage(),
                    HttpStatus.NOT_FOUND.toString()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDetails);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<DatasetMetadata>> getAllDatasets(@PageableDefault(size = 20, sort = {"datasetID"}) Pageable pageable) {
        var datasets = datasetsService.getAllDatasets(pageable);
        return ResponseEntity.ok(datasets);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDataset(@PathVariable String id) {
        try {
            datasetsService.deleteDataset(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/trending")
    public ResponseEntity<Page<DatasetMetadata>> trending(@PageableDefault(size = 20) Pageable pageable) {
        Page<DatasetMetadata> trendingDatasets = datasetsService.trending(pageable);
        return ResponseEntity.ok(trendingDatasets);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadDataset(@PathVariable String id) {
        try {
            DatasetDownloadData datasetDownloadData = datasetsService.downloadDataset(id);
            var multipartFile = datasetDownloadData.getFile();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(Objects.requireNonNull(multipartFile.getContentType())))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + multipartFile.getOriginalFilename() + "\"")
                    .body(new InputStreamResource(multipartFile.getInputStream()));
        } catch (RuntimeException e) {
            var responseDetails = new ResponseDetails<>(
                    LocalDateTime.now(),
                    e.getMessage(),
                    HttpStatus.NOT_FOUND.toString()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDetails);
        } catch (IOException e) {
            var responseDetails = new ResponseDetails<>(
                    LocalDateTime.now(),
                    "Error transferring the file",
                    HttpStatus.REQUEST_TIMEOUT.toString()
            );
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(responseDetails);
        }
    }
}