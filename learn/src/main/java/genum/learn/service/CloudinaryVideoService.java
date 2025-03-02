package genum.learn.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@AllArgsConstructor
@Service
public class CloudinaryVideoService implements VideoService{

    private final Cloudinary cloudinary;
    @Override
    public String uploadVideo(MultipartFile file) throws IOException {
        var uploadResult = cloudinary.uploader().uploadLarge(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", file.getOriginalFilename(),
                        "overwrite", false,
                        "resource_type", "video"
                )
        );
        return uploadResult.get("secure_url").toString();
    }
}
