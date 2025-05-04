package genum.app.dataset;

import genum.app.shared.BaseDatabaseIntegration;
import genum.dataset.DTO.CreateDatasetRequest;
import genum.dataset.DTO.DatasetUpdateRequest;
import genum.dataset.domain.Collaborator;
import genum.dataset.domain.Tag;
import genum.dataset.enums.*;
import genum.dataset.model.Dataset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class DataSetIT extends BaseDatabaseIntegration {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MongoTemplate datasetTemplate;
    @Autowired
    private JacksonTester<CreateDatasetRequest> validJsonCreateDatasetRequest;
    @Autowired
    private JacksonTester<CreateDatasetRequest> invalidJsonCreateDatasetRequest;

    private final CreateDatasetRequest validCreateDatasetRequest = new CreateDatasetRequest("datasetName",
            Visibility.PUBLIC.getValue());
    private final CreateDatasetRequest invalidCreateDatasetRequest = new CreateDatasetRequest("datasetName",
            "invalid");

    /* ================================================== Test for creation of dataset ============================== */

    @Test
    void givenValidFileAndRequestBodyShouldCreateADatabase() throws Exception {
        MockMultipartFile metadataPart = new MockMultipartFile(
                "metadata",
                "metadata",
                "application/json",
                validJsonCreateDatasetRequest.write(validCreateDatasetRequest).getJson().getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "data.csv",
                "text/csv",
                "a,b,c\n1,2,3".getBytes(StandardCharsets.UTF_8)
        );
        mockMvc.perform(
                multipart("/api/dataset/upload")
                        .file(metadataPart)
                        .file(filePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user("test_user@email.com").roles("USER"))
        ).andExpect(status().isCreated());
    }
    @Test
    void givenInvalidRequestBodyShouldReturn400() throws Exception {
        MockMultipartFile metadataPart = new MockMultipartFile(
                "metadata",
                "metadata",
                "application/json",
                invalidJsonCreateDatasetRequest.write(invalidCreateDatasetRequest).getJson().getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "data.csv",
                "text/csv",
                "a,b,c\n1,2,3".getBytes(StandardCharsets.UTF_8)
        );
        mockMvc.perform(
                multipart("/api/dataset/upload")
                        .file(metadataPart)
                        .file(filePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user("test_user@email.com").roles("USER"))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void givenInvalidFileShouldReturn400() throws Exception {
        MockMultipartFile metadataPart = new MockMultipartFile(
                "metadata",
                "metadata",
                "application/json",
                invalidJsonCreateDatasetRequest.write(invalidCreateDatasetRequest)
                        .getJson().getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "data.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                new byte[]{}
        );
        mockMvc.perform(
                multipart("/api/dataset/upload")
                        .file(metadataPart)
                        .file(filePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user("test_user@email.com").roles("USER"))
        ).andExpect(status().isBadRequest());
    }

    /* =========================================== Test for updating dataset =========================================*/

    @Autowired
    private JacksonTester<DatasetUpdateRequest> validJsonDataSetUpdateRequest;
    private final DatasetUpdateRequest validDatasetUpdateRequest = DatasetUpdateRequest.builder()
            .tags(Set.of(
                    new Tag(TagsEnum.CRIME.getValue(), TagsEnum.CRIME.getTagCategory()),
                    new Tag(TagsEnum.ACCELERATORS.getValue(), TagsEnum.ACCELERATORS.getTagCategory())
            ))
            .subtitle("subtitle")
            .description("Lorem Ipsum is damn dope")
            .visibility(Visibility.PRIVATE.getValue())
            .build();
    @Test
    void givenADatasetAlreadyExistingShouldBeUpdatedAndReturn200() throws Exception {
        var uploaderId = UUID.randomUUID().toString();
        Dataset existingDataset = Dataset.builder()
                .datasetID(UUID.randomUUID().toString())
                .tags(Set.of(new Tag(TagsEnum.ACCELERATORS.getValue(),TagsEnum.ACCELERATORS.getTagCategory())))
                .datasetName("test dataset")
                .datasetType(DatasetType.CSV)
                .uploaderId(uploaderId)
                .visibility(Visibility.PUBLIC)
                .collaborators(Set.of(new Collaborator("divjazz",uploaderId, CollaboratorPermission.OWNER)))
                .build();
        datasetTemplate.save(existingDataset,"dataset");

        mockMvc.perform(
                put("/api/dataset/update/%s".formatted(existingDataset.getDatasetID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJsonDataSetUpdateRequest.write(validDatasetUpdateRequest).getJson())
        ).andExpect(status().isCreated());
    }

    @Autowired
    private JacksonTester<DatasetUpdateRequest> invalidJsonDataSetUpdateRequest;


    private final DatasetUpdateRequest invalidDatasetUpdateRequest = DatasetUpdateRequest.builder()
            .tags(Set.of(
                    new Tag(TagsEnum.CRIME.getValue(), TagsEnum.CRIME.getTagCategory()),
                    new Tag(TagsEnum.ACCELERATORS.getValue(), TagsEnum.ACCELERATORS.getTagCategory())
            ))
            .subtitle("dsnsdnsk")
            .description("Lorem Ipsum is damn dope")
            .visibility("invalid")
            .build();


    @Test
    void givenADatasetAlreadyExistingAndInvalidUpdateRequestShouldReturn400() throws Exception {
        var uploaderId = UUID.randomUUID().toString();
        Dataset existingDataset = Dataset.builder()
                .datasetID(UUID.randomUUID().toString())
                .tags(Set.of(new Tag(TagsEnum.ACCELERATORS.getValue(),TagsEnum.ACCELERATORS.getTagCategory())))
                .datasetName("test dataset")
                .datasetType(DatasetType.CSV)
                .uploaderId(uploaderId)
                .visibility(Visibility.PUBLIC)
                .collaborators(Set.of(new Collaborator("divjazz",uploaderId, CollaboratorPermission.OWNER)))
                .build();
        datasetTemplate.save(existingDataset,"dataset");

        mockMvc.perform(
                put("/api/dataset/update/%s".formatted(existingDataset.getDatasetID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonDataSetUpdateRequest.write(invalidDatasetUpdateRequest).getJson())
        ).andExpect(status().isBadRequest());
    }


}
