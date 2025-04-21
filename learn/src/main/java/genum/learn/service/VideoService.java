package genum.learn.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;


public interface VideoService {
    String uploadVideo(MultipartFile file) throws IOException;
    String uploadVideo(Path path) throws IOException;
    boolean deleteVideo(String url);
}
