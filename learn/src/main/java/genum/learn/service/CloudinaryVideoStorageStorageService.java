package genum.learn.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import genum.learn.model.Video;
import genum.shared.upload.CloudinaryResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@AllArgsConstructor
@Service
public class CloudinaryVideoStorageStorageService implements VideoStorageService {

    private final Cloudinary cloudinary;
    private final ObjectMapper objectMapper;
    @Override
    public String uploadVideo(MultipartFile file, Video video) throws IOException {
        var uploadResult = cloudinary.uploader().uploadLarge(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", video.getTitle()+video.getVideoId(),
                        "overwrite", false,
                        "resource_type", "video"
                )
        );
        return objectMapper.convertValue(uploadResult, CloudinaryResponse.class).secureUrl();
    }

    @Override
    public String uploadVideo(Path path, Video video) throws IOException {
        var uploadResult = cloudinary.uploader().uploadLarge(
                Files.readAllBytes(path),
                ObjectUtils.asMap(
                        "public_id", video.getTitle()+video.getVideoId(),
                        "overwrite", false,
                        "resource_type", "video"
                )
        );
        return objectMapper.convertValue(uploadResult, CloudinaryResponse.class).secureUrl();
    }

    @Override
    public boolean deleteVideo(Video video){
        String publicUrl = video.getTitle()+video.getVideoId();
        try {
            var deleteResult = cloudinary.uploader().destroy(publicUrl, ObjectUtils.asMap("resource_type", "video"));
            return "ok".equals(deleteResult.get("result"));
        } catch (IOException e) {
            return false;
        }
    }
}
