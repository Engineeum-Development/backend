package genum.dataset.controller;

import genum.dataset.DTO.CreateDatasetRequest;
import genum.dataset.DTO.DatasetDTO;
import genum.dataset.DTO.DatasetUpdateRequest;
import genum.dataset.domain.License;
import genum.dataset.domain.Tag;
import genum.dataset.service.DatasetService;
import genum.shared.DTO.response.PageResponse;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.dataset.exception.DatasetNotFoundException;
import genum.shared.exception.UploadSizeLimitExceededException;
import genum.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/dataset")
@Slf4j
@RequiredArgsConstructor
public class DatasetController {


    private final DatasetService datasetService;
    @Value("${dataset.upload.max-file-size}")
    private String maxUploadSize;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createDataset(@Valid @RequestPart("metadata") CreateDatasetRequest createDatasetRequest,
                                           @RequestPart("file") MultipartFile file) {

        var MAX_FILE_SIZE = DataSize.parse(maxUploadSize);
        var file_data_size = DataSize.ofBytes(file.getSize());

        if (file_data_size.toBytes() > MAX_FILE_SIZE.toBytes()) {
            throw new UploadSizeLimitExceededException(file_data_size.toBytes(), MAX_FILE_SIZE.toBytes());
        }
        try {
            var createdDatasetId = datasetService.createDataset(createDatasetRequest, file);
            var responseDetails = new ResponseDetails<>(
                    LocalDateTime.now(),
                    "Dataset created successfully.",
                    HttpStatus.CREATED.toString(),
                    Map.of("datasetId", createdDatasetId)
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
    }

    @PreAuthorize("""
        @datasetPermissionEvaluator.isUserPermitted(#updatedDataset)
        """)
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDataset(@PathVariable String id, @Valid @RequestBody DatasetUpdateRequest updatedDataset) {
        var datasetDTO = datasetService.updateDataset(id, updatedDataset);
        var responseDetails = new ResponseDetails<>(
                LocalDateTime.now(),
                "Dataset updated successfully.",
                HttpStatus.OK.toString(),
                datasetDTO
        );
        return ResponseEntity.ok(responseDetails);
    }

    @GetMapping("/all/{id}")
    public ResponseEntity<ResponseDetails<DatasetDTO>> getDatasetById(@PathVariable String id) {

        DatasetDTO dataset = datasetService.getDatasetDTOById(id);
        ResponseDetails<DatasetDTO> responseDetails = new ResponseDetails<>(
                LocalDateTime.now(),
                "Dataset found",
                HttpStatus.OK.toString(),
                dataset
        );
        return ResponseEntity.ok(responseDetails);
    }

    @GetMapping("/all")
    public ResponseEntity<PageResponse<DatasetDTO>> getAllDatasets(@PageableDefault(size = 20, sort = {"name"}) Pageable pageable) {
        var datasets = datasetService.getAllDatasets(pageable);
        return ResponseEntity.ok(datasets);
    }

    @DeleteMapping("/{datasetId}")
    public ResponseEntity<Void> deleteDataset(@PathVariable String datasetId) {
        try {
            datasetService.deleteDataset(datasetId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (DatasetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @GetMapping("/trending")
    public ResponseEntity<PageResponse<DatasetDTO>> trending(@PageableDefault(size = 20) Pageable pageable) {
        var trendingDatasets = datasetService.trending(pageable);
        return ResponseEntity.ok(trendingDatasets);
    }

    @GetMapping("/tag")
    public ResponseEntity<ResponseDetails<Set<Tag>>> getAllTags(){
        var responseDetails = new ResponseDetails<>("tags", HttpStatus.OK.toString(), datasetService.getAllTags());
        return ResponseEntity.ok(responseDetails);
    }
    @GetMapping("/license")
    public ResponseEntity<ResponseDetails<Set<License>>> getAllLicenses(){
        var responseDetails = new ResponseDetails<>("licenses", HttpStatus.OK.toString(), datasetService.getAllLicences());
        return ResponseEntity.ok(responseDetails);
    }

    @GetMapping("/download/{datasetId}")
    public ResponseEntity<?> downloadDataset(@PathVariable String datasetId) {
        try {
            String datasetDownloadURL = datasetService.downloadDataset(datasetId);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(datasetDownloadURL)).build();
        } catch (DatasetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/upvote/{datasetId}")
    public ResponseEntity<ResponseDetails<DatasetDTO>> upvoteDataset(@PathVariable String datasetId) {
        var datasetDTO = datasetService.upvoteDataset(datasetId);
        return ResponseEntity.ok(new ResponseDetails<>("upvoted",HttpStatus.OK.toString(),datasetDTO));
    }
}