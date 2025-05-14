package genum.learn.service;

import genum.learn.model.Video;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;


public interface VideoStorageService {
    String uploadVideo(MultipartFile file, Video video) throws IOException;
    String uploadVideo(Path path, Video video) throws IOException;
    boolean deleteVideo(Video video);
}
