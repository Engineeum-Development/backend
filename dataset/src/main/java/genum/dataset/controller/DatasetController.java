package genum.dataset.controller;

import genum.dataset.DTO.CreateDatasetDTO;
import genum.dataset.domain.DatasetMetadata;
import genum.dataset.service.DatasetsServiceImpl;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.dataset.exception.DatasetNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/dataset")
@Slf4j
@RequiredArgsConstructor
public class DatasetController {


    private final DatasetsServiceImpl datasetsService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    public ResponseEntity<?> createDataset(@Valid @RequestPart("metadata") CreateDatasetDTO createDatasetDTO,
                                           @RequestPart("file") MultipartFile file) {
        try {
            var createdDataset = datasetsService.createDataset(createDatasetDTO, file);
            var responseDetails = new ResponseDetails<>(
                    LocalDateTime.now(),
                    "Dataset created successfully.",
                    HttpStatus.CREATED.toString(),
                    Map.of("datasetUrl", createdDataset)
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDetails);
        } catch (IOException e) {
            var responseDetails = new ResponseDetails<>(
                    LocalDateTime.now(),
                    e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE.toString()
            );
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseDetails);
        }
        catch (IllegalArgumentException e) {
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
            DatasetMetadata updated = datasetsService.updateDatasetMetadata(id, updatedDataset);
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
            return ResponseEntity.ok(responseDetails);
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
    public ResponseEntity<Page<DatasetMetadata>> getAllDatasets(@PageableDefault(size = 20, sort = {"datasetId"}) Pageable pageable) {
        var datasets = datasetsService.getAllDatasets(pageable);
        return ResponseEntity.ok(datasets);
    }

    @DeleteMapping("/delete/{datasetId}")
    public ResponseEntity<?> deleteDataset(@PathVariable String datasetId) {
        try {
            datasetsService.deleteDataset(datasetId);
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

    @GetMapping("/download/{datasetId}")
    public ResponseEntity<?> downloadDataset(@PathVariable String datasetId) {
        try {
            String datasetDownloadURL = datasetsService.downloadDataset(datasetId);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(datasetDownloadURL)).build();
        } catch (DatasetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @PutMapping("/like/{datasetId}")
    public ResponseEntity<?> likeDataset(@PathVariable String datasetId){
            datasetsService.likeDataset(datasetId);
            return ResponseEntity.noContent().build();
    }
}