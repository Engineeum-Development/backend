package genum.dataset.service;

import genum.dataset.DTO.CreateDatasetRequest;
import genum.dataset.DTO.DatasetUpdateRequest;
import genum.dataset.domain.DatasetMetadata;
import genum.dataset.domain.PendingActions;
import genum.dataset.enums.DatasetType;
import genum.dataset.enums.Visibility;
import genum.dataset.model.Dataset;
import genum.dataset.repository.DatasetRepository;
import genum.genumUser.repository.GenumUserRepository;
import genum.genumUser.repository.projection.GenumUserWithIDFirstNameLastName;
import genum.shared.DTO.response.PageResponse;
import genum.shared.dataset.exception.DatasetNotFoundException;
import genum.shared.genumUser.exception.BadRequestException;
import genum.shared.security.SecurityUtils;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class DatasetServiceTest {
    private final Dataset datasetReturned = Dataset.builder()
            .datasetID(UUID.randomUUID().toString())
            .datasetName("datasetName")
            .datasetType(DatasetType.CSV)
            .visibility(Visibility.PRIVATE)
            .pendingActions(PendingActions.pendingActions)
            .tags(new HashSet<>())
            .authors(new HashSet<>())
            .collaborators(new HashSet<>())
            .usersThatUpvote(new HashSet<>())
            .filePublicId("url to download file")
            .build();
    private final CreateDatasetRequest createDatasetRequest = new CreateDatasetRequest(
            "datasetName", Visibility.PRIVATE.getValue()
    );
    private final DatasetUpdateRequest validDatasetUpdateRequest = new DatasetUpdateRequest(
            Set.of(),
            null,
            "citation",
            "Added Subtitle",
            null,
            "Some little description",
            "public",
            Set.of(),
            Set.of(),
            null
    );
    private final DatasetUpdateRequest invalidDatasetUpdateRequest = new DatasetUpdateRequest(
            Set.of(),
            null,
            "citation",
            "Added Subtitle",
            null,
            "Some little description",
            "publico",
            Set.of(),
            Set.of(),
            null
    );
    private final Dataset updatedDataset = Dataset.builder()
            .datasetID(datasetReturned.getDatasetID())
            .datasetName(datasetReturned.getDatasetName())
            .datasetType(datasetReturned.getDatasetType())
            .visibility(Visibility.PUBLIC)
            .tags(new HashSet<>())
            .authors(new HashSet<>())
            .collaborators(new HashSet<>())
            .datasetSubtitle(validDatasetUpdateRequest.subtitle())
            .description(validDatasetUpdateRequest.description())
            .doiCitation(validDatasetUpdateRequest.doiCitation())
            .usersThatUpvote(new HashSet<>())
            .build();
    @InjectMocks
    private DatasetService datasetService;
    @Mock
    private DatasetRepository datasetsRepository;
    @Mock
    private DatasetStorageService datasetStorageService;
    @Mock
    private GenumUserRepository genumUserRepository;
    @Mock
    private SecurityUtils securityUtils;
    @Captor
    private ArgumentCaptor<Dataset> datasetArgumentCaptor;


    /* ========================================================== Test for createDataset() ===========================*/

    @Test
    void shouldCreateDataset() throws IOException {
        String userId = UUID.randomUUID().toString();
        given(securityUtils.getCurrentAuthenticatedUserId()).willReturn(userId);
        given(genumUserRepository.findByCustomUserDetails_UserReferenceIdReturningIdFirstAndName(userId))
                .willReturn(
                        List.of(new GenumUserWithIDFirstNameLastName(userId,
                                "Firstname",
                                "Lastname"))
                );
        given(datasetStorageService.storeDataSet(any(MultipartFile.class), any(DatasetMetadata.class))).willReturn("downloadUrl");
        given(datasetsRepository.save(any(Dataset.class))).willReturn(datasetReturned);
        try (BufferedInputStream is = new BufferedInputStream(new ClassPathResource("test.json").getInputStream())) {
            var returnedString = datasetService.createDataset(createDatasetRequest, new MockMultipartFile("test", "test.json", null, is.readAllBytes()));
            assertThat(returnedString).isEqualTo(datasetReturned.getDatasetID());
        }

        then(datasetStorageService).should(times(1)).storeDataSet(any(MultipartFile.class), any(DatasetMetadata.class));
        then(genumUserRepository).should(times(1)).findByCustomUserDetails_UserReferenceIdReturningIdFirstAndName(anyString());
        then(datasetsRepository).should(times(1)).save(any(Dataset.class));
    }

    @Test
    void shouldThrowBadRequestExceptionForUnsupportedFileFormat() throws IOException {
        String userId = UUID.randomUUID().toString();
        given(securityUtils.getCurrentAuthenticatedUserId()).willReturn(userId);
        given(genumUserRepository.findByCustomUserDetails_UserReferenceIdReturningIdFirstAndName(userId))
                .willReturn(
                        List.of(new GenumUserWithIDFirstNameLastName(userId,
                                "Firstname",
                                "Lastname"))
                );
        try (BufferedInputStream is = new BufferedInputStream(new ClassPathResource("test.json").getInputStream())) {

            assertThatExceptionOfType(BadRequestException.class)
                    .isThrownBy(() -> datasetService
                            .createDataset(createDatasetRequest, new MockMultipartFile("test",
                                    "test.mp4",
                                    null,
                                    is.readAllBytes()))).withMessage("Invalid parameters found: Invalid file type. Only JSON and CSV are allowed.");
        }

        then(datasetStorageService).should(never()).storeDataSet(any(MultipartFile.class), any(DatasetMetadata.class));
        then(genumUserRepository).should(times(1))
                .findByCustomUserDetails_UserReferenceIdReturningIdFirstAndName(anyString());
        then(datasetsRepository).should(never()).save(any(Dataset.class));
    }

    /* ======================================================== Test for updateDataset ===============================*/
    @Test
    void shouldUpdateDatasetGivenDatasetUpdateRequest() {
        given(datasetsRepository.getDatasetByDatasetID(anyString())).willReturn(Optional.of(datasetReturned));
        given(datasetsRepository.save(any(Dataset.class))).willReturn(updatedDataset);

        datasetService.updateDataset(datasetReturned.getDatasetID(), validDatasetUpdateRequest);

        then(datasetsRepository).should(times(1)).save(datasetArgumentCaptor.capture());
        var capturedDataset = datasetArgumentCaptor.getValue();
        assertThat(validDatasetUpdateRequest.doiCitation()).isEqualTo(capturedDataset.getDoiCitation());
        assertThat(validDatasetUpdateRequest.subtitle()).isEqualTo(capturedDataset.getDatasetSubtitle());
        assertThat(validDatasetUpdateRequest.description()).isEqualTo(capturedDataset.getDescription());
        assertThat(validDatasetUpdateRequest.visibility())
                .isEqualToIgnoringCase(
                        capturedDataset.getVisibility()
                                .getValue()
                );
    }

    @Test
    void shouldThrowBadRequestGivenInvalidDatasetUpdateRequest() {
        given(datasetsRepository.getDatasetByDatasetID(anyString())).willReturn(Optional.of(datasetReturned));

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> datasetService.updateDataset(datasetReturned.getDatasetID(), invalidDatasetUpdateRequest));

        then(datasetsRepository).should(never()).save(any(Dataset.class));
    }

    /* ================================================= Tests for getDatasetDTOById ================================ */

    @Test
    void shouldGetDatasetById() {
        given(datasetsRepository.findDatasetDTObyDatasetID(anyString())).willReturn(Optional.of(datasetReturned.toDTO()));

        var datasetDto = datasetService.getDatasetDTOById(UUID.randomUUID().toString());
        assertThat(datasetDto.datasetId()).isEqualTo(datasetReturned.getDatasetID());
    }

    @Test
    void shouldThrowDatasetNotFound() {
        given(datasetsRepository.findDatasetDTObyDatasetID(anyString())).willReturn(Optional.empty());

        assertThatExceptionOfType(DatasetNotFoundException.class)
                .isThrownBy(() -> datasetService.getDatasetDTOById(UUID.randomUUID().toString()));

    }


    /* ================================================= Tests for getAllDatasets() ================================= */
    @Test
    void shouldGetPagedDatasets() {
        given(datasetsRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(List.of(datasetReturned)));

        var page = datasetService.getAllDatasets(Pageable.ofSize(10));

        assertThat(page).isNot(new Condition<>(PageResponse::empty,"Page should not be empty"));

        then(datasetsRepository).should(times(1)).findAll(any(Pageable.class));
    }

    /* ================================================== Test for deleteDataset() ================================== */
    @Test
    void shouldDeleteDatesetById() throws IOException {
        given(datasetsRepository.getDatasetByDatasetID(anyString())).willReturn(Optional.of(datasetReturned));
        given(datasetStorageService.deleteDataset(anyString())).willReturn("deleted");

        datasetService.deleteDataset(UUID.randomUUID().toString());

        then(datasetStorageService).should(times(1)).deleteDataset(anyString());
        then(datasetsRepository).should(times(1)).getDatasetByDatasetID(anyString());

    }

    @Test
    void shouldThrowDatasetNotFoundExceptionIfNotFound() throws IOException {
        given(datasetsRepository.getDatasetByDatasetID(anyString())).willReturn(Optional.empty());

        assertThatExceptionOfType(DatasetNotFoundException.class)
                .isThrownBy(() -> datasetService.deleteDataset(UUID.randomUUID().toString()));

        then(datasetStorageService).should(never()).deleteDataset(anyString());
        then(datasetsRepository).should(never()).deleteByDatasetID(anyString());
    }

    /* ======================================== Tests for trending() ================================================ */



}
