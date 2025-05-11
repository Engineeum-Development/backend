package genum.app.dataset;

import genum.app.shared.BaseDatabaseIntegration;
import genum.dataset.DTO.CreateDatasetRequest;
import genum.dataset.DTO.DatasetDTO;
import genum.dataset.DTO.DatasetUpdateRequest;
import genum.dataset.domain.Collaborator;
import genum.dataset.domain.Tag;
import genum.dataset.enums.CollaboratorPermission;
import genum.dataset.enums.DatasetType;
import genum.dataset.enums.TagsEnum;
import genum.dataset.enums.Visibility;
import genum.dataset.model.Dataset;
import genum.genumUser.model.GenumUser;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.constant.Gender;
import genum.shared.security.CustomUserDetails;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
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


    private CustomUserDetails userDetails;

    private final CreateDatasetRequest validCreateDatasetRequest = new CreateDatasetRequest("datasetName",
            Visibility.PUBLIC.getValue());
    private final CreateDatasetRequest invalidCreateDatasetRequest = new CreateDatasetRequest("datasetName",
            "invalid");
    private GenumUser genumUser;

    /* ================================================== Test for creation of dataset ============================== */

    @BeforeEach
    void setupSecurity() {
        userDetails = new CustomUserDetails("some password", "some.gmail.com");
        userDetails.setUserReferenceId(UUID.randomUUID().toString());
        genumUser = GenumUser.builder()
                .gender(Gender.MALE)
                .createdDate(LocalDateTime.now())
                .isVerified(true)
                .lastName("Main")
                .firstName("Beast")
                .customUserDetails(userDetails)
                .country("Nigeria")
                .build();
        datasetTemplate.save(genumUser, "users");
        SecurityContextHolder.setContext(
                new SecurityContextImpl(UsernamePasswordAuthenticationToken.authenticated(
                        userDetails,
                        "password"
                        , List.of(new SimpleGrantedAuthority("ROLE_USER")
                        ))
                )
        );
    }
    @AfterEach
    void remove() {
        datasetTemplate.remove(genumUser, "users");
    }
    @Test
    void givenValidFileAndRequestBodyShouldCreateADataset() throws Exception {
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
                        .with(user(userDetails))
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
                        .with(user(userDetails))
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
                        .with(user(userDetails))
        ).andExpect(status().isBadRequest());
    }

    /* =========================================== Test for updating dataset =========================================*/

    @Autowired
    private JacksonTester<DatasetUpdateRequest> validJsonDataSetUpdateRequest;

    @Test
    void givenADatasetAlreadyExistingShouldBeUpdatedAndReturn200() throws Exception {
        var uploaderId = genumUser.getCustomUserDetails().getUserReferenceId();

        DatasetUpdateRequest validDatasetUpdateRequest = DatasetUpdateRequest.builder()
                .tags(Set.of(
                        new Tag(TagsEnum.CRIME.getValue(), TagsEnum.CRIME.getTagCategory()),
                        new Tag(TagsEnum.ACCELERATORS.getValue(), TagsEnum.ACCELERATORS.getTagCategory())
                ))
                .subtitle("subtitle")
                .description("Lorem Ipsum is damn dope")
                .visibility(Visibility.PRIVATE.getValue())
                .collaborators(Set.of(new Collaborator("Beast", userDetails.getUserReferenceId(), CollaboratorPermission.OWNER)))
                .build();
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
                        .with(user(genumUser.getCustomUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJsonDataSetUpdateRequest.write(validDatasetUpdateRequest).getJson())
        ).andExpect(status().isOk());
    }

    @Autowired
    private JacksonTester<DatasetUpdateRequest> invalidJsonDataSetUpdateRequest;



    @Test
    void givenADatasetAlreadyExistingAndInvalidUpdateRequestShouldReturn400() throws Exception {
        var uploaderId = genumUser.getCustomUserDetails().getUserReferenceId();

        DatasetUpdateRequest invalidDatasetUpdateRequest = DatasetUpdateRequest.builder()
                .tags(Set.of(
                        new Tag(TagsEnum.CRIME.getValue(), TagsEnum.CRIME.getTagCategory()),
                        new Tag(TagsEnum.ACCELERATORS.getValue(), TagsEnum.ACCELERATORS.getTagCategory())
                ))
                .subtitle("dsnsdnsk")
                .description("Lorem Ipsum is damn dope")
                .visibility("invalid")
                .collaborators(Set.of(new Collaborator("divjazz",uploaderId, CollaboratorPermission.OWNER)))
                .build();

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
                        .with(user(genumUser.getCustomUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonDataSetUpdateRequest.write(invalidDatasetUpdateRequest).getJson())
        ).andExpect(status().isBadRequest());
    }

    /* ======================================= Test for getting dataset by id ========================================*/
    @Autowired
    private JacksonTester<ResponseDetails<DatasetDTO>> jsonDatasetDTOResponse;
    @Test
    void givenDatasetExistsShouldGetDatasetByIdAndReturn200() throws Exception {
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

        var response = mockMvc.perform(
                get("/api/dataset/all/%s".formatted(existingDataset.getDatasetID()))
        ).andExpect(status().isOk()).andReturn().getResponse();

        try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            ResponseDetails<DatasetDTO> responseDetails = jsonDatasetDTOResponse
                    .read(bis).getObject();
            Assertions.assertThat(responseDetails.getData().datasetId()).isEqualTo(existingDataset.getDatasetID());
        }


    }

    @Test
    void givenDatasetNotExistsShouldReturn404NotFound() throws Exception {
        mockMvc.perform(
                get("/api/dataset/all/%s".formatted(UUID.randomUUID().toString()))
        ).andExpect(status().isNotFound());
    }

    /* ========================================== Test for getting datasets ========================================= */

    @Test
    void givenDatasetsExistOrNotShouldReturn200OK() throws Exception {
        mockMvc.perform(
                get("/api/dataset/all")
        ).andExpect(status().isOk());
    }

    /* ========================================== Test for deleting datasets ======================================== */

    @Test
    void givenDatasetExistsShouldDeleteDataset() throws Exception {
        var uploaderId = UUID.randomUUID().toString();
        Dataset existingDataset = Dataset.builder()
                .datasetID(UUID.randomUUID().toString())
                .tags(Set.of(new Tag(TagsEnum.ACCELERATORS.getValue(),TagsEnum.ACCELERATORS.getTagCategory())))
                .datasetName("test dataset")
                .datasetType(DatasetType.CSV)
                .uploaderId(uploaderId)
                .visibility(Visibility.PUBLIC)
                .filePublicId("filePublicId")
                .collaborators(Set.of(new Collaborator("divjazz",uploaderId, CollaboratorPermission.OWNER)))
                .build();

        var dataset = datasetTemplate.save(existingDataset,"dataset");
        mockMvc.perform(
                delete("/api/dataset/%s".formatted(existingDataset.getDatasetID()))
                        .with(user(userDetails))
        ).andExpect(status().isOk());

        Assertions.assertThat(datasetTemplate.findById(dataset.getId(), Dataset.class, "dataset")).isNull();
    }

    @Test
    void givenDatasetNotExistingShouldReturnNoContent204() throws Exception {
        var userDetails = new CustomUserDetails("some password", "some.gmail.com");
        userDetails.setUserReferenceId(UUID.randomUUID().toString());
        mockMvc.perform(
                delete("/api/dataset/%s".formatted(UUID.randomUUID()))
                        .with(user(userDetails))
        ).andExpect(status().isNoContent());
    }

    /* ===================================== Test for getting trending datasets ===================================== */

    @Test
    void shouldReturnTrendingDatasets() throws Exception {
        mockMvc.perform(
                get("/api/dataset/trending")
        ).andExpect(status().isOk());
    }

    /* ====================================== Test getting all tags ================================================= */
    @Test
    void shouldGetAllTagsOffered() throws Exception {
        mockMvc.perform(
                get("/api/dataset/tag")
        ).andExpect(status().isOk());
    }

    /* ====================================== Test getting all licenses ============================================= */

    @Test
    void shouldGetAllLicensesOffered() throws Exception {
        mockMvc.perform(
                get("/api/dataset/license")
        ).andExpect(status().isOk());
    }

    /* ====================================== Test Dataset Download ================================================= */
    @Autowired
    private JacksonTester<ResponseDetails<Map<String, String>>> datasetCreationResponse;

    void givenADatasetThatExistsShouldBeAbleToDownload() throws Exception {
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


        MockHttpServletResponse response = mockMvc.perform(
                multipart("/api/dataset/upload")
                        .file(metadataPart)
                        .file(filePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user(userDetails))
        ).andExpect(status().isCreated()).andReturn().getResponse();

        Map<String, String> responseMap = datasetCreationResponse
                .parse(response.getContentAsString(StandardCharsets.UTF_8))
                .getObject().getData();
        var datasetId = responseMap.get("datasetId");


        mockMvc.perform(
                get("/api.dataset/download/%s".formatted(datasetId))
        ).andExpect(status().isFound());




    }



}
