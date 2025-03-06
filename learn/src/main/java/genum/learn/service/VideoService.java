package genum.learn.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public interface VideoService {
    String uploadVideo(MultipartFile file) throws IOException;
    boolean deleteVideo(String url);
}
