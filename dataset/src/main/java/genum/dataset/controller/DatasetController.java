package genum.dataset.controller;
import genum.dataset.DTO.CreateDatasetDTO;
import genum.dataset.model.Datasets;
import genum.dataset.service.DatasetsServiceImpl;
import genum.shared.DTO.response.ResponseDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/datasets")
@Slf4j
@RequiredArgsConstructor
public class DatasetController {


    private final DatasetsServiceImpl datasetsService;

    @PostMapping("/create")
    public ResponseEntity<?> createDataset(@Valid @RequestBody CreateDatasetDTO createDatasetDTO) {
        try {
            Datasets createdDataset = datasetsService.createDataset(createDatasetDTO);
            ResponseDetails responseDetails = new ResponseDetails(
                    LocalDateTime.now(),
                    "Dataset created successfully.",
                    HttpStatus.CREATED.toString()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDetails);
        } catch (Exception e) {
            ResponseDetails responseDetails = new ResponseDetails(
                    LocalDateTime.now(),
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.toString()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDetails);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDataset(@PathVariable String id, @Valid @RequestBody Datasets updatedDataset) {
        try {
            Datasets updated = datasetsService.updateDataset(id, updatedDataset);
            ResponseDetails responseDetails = new ResponseDetails(
                    LocalDateTime.now(),
                    "Dataset updated successfully.",
                    HttpStatus.OK.toString()
            );
            return ResponseEntity.ok(responseDetails);
        } catch (Exception e) {
            ResponseDetails responseDetails = new ResponseDetails(
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
            Datasets dataset = datasetsService.getDatasetById(id);
            return ResponseEntity.ok(dataset);
        } catch (RuntimeException e) {
            ResponseDetails responseDetails = new ResponseDetails(
                    LocalDateTime.now(),
                    e.getMessage(),
                    HttpStatus.NOT_FOUND.toString()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDetails);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Datasets>> getAllDatasets() {
        List<Datasets> datasets = datasetsService.getAllDatasets();
        return ResponseEntity.ok(datasets);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDataset(@PathVariable String id) {
        try {
            datasetsService.deleteDataset(id);
            ResponseDetails responseDetails = new ResponseDetails(
                    LocalDateTime.now(),
                    "Dataset deleted successfully.",
                    HttpStatus.OK.toString()
            );
            return ResponseEntity.ok(responseDetails);
        } catch (RuntimeException e) {
            ResponseDetails responseDetails = new ResponseDetails(
                    LocalDateTime.now(),
                    e.getMessage(),
                    HttpStatus.NOT_FOUND.toString()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDetails);
        }
    }

    @GetMapping("/trending")
    public ResponseEntity<List<Datasets>> trending() {
        List<Datasets> trendingDatasets = datasetsService.trending();
        return ResponseEntity.ok(trendingDatasets);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadDataset(@PathVariable String id) {
        try {
            datasetsService.downloadDataset(id);
            ResponseDetails responseDetails = new ResponseDetails(
                    LocalDateTime.now(),
                    "File download initiated.",
                    HttpStatus.OK.toString()
            );
            return ResponseEntity.ok(responseDetails);
        } catch (RuntimeException e) {
            ResponseDetails responseDetails = new ResponseDetails(
                    LocalDateTime.now(),
                    e.getMessage(),
                    HttpStatus.NOT_FOUND.toString()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDetails);
        }
    }
}