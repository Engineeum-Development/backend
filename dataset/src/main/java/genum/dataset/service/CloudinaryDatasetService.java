package genum.dataset.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import genum.dataset.domain.DatasetMetadata;
import genum.shared.upload.CloudinaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CloudinaryDatasetService implements DatasetStorageService {

    private final Cloudinary cloudinary;
    private final ObjectMapper objectMapper;

    @Override
    public String storeDataSet(MultipartFile file, DatasetMetadata metadata) throws IOException {
        var uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "raw",
                        "overwrite", true,
                        "public_id", metadata.getDatasetName()+metadata.getUserId()
                )

        );
        return objectMapper.convertValue(uploadResult, CloudinaryResponse.class).secureUrl();
    }

    @Override
    public String deleteDataset(String id) throws IOException {
        return (String) cloudinary.uploader().destroy(id, ObjectUtils.emptyMap()).get("result");
    }
}
