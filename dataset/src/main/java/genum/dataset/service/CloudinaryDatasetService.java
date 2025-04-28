package genum.dataset.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import genum.dataset.domain.DatasetMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CloudinaryDatasetService implements DatasetStorageService {

    private final Cloudinary cloudinary;

    @Override
    public String storeDataSet(MultipartFile file, DatasetMetadata metadata) throws IOException {
        var uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "raw",
                        "overwrite", true,
                        "public_id", file.getOriginalFilename()
                )

        );
        return uploadResult.get("secure_url").toString();
    }

    @Override
    public String deleteDataset(String id) throws IOException {
        return "deleted";
    }
}
