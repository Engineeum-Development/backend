package genum.dataset.service;

import genum.dataset.domain.DatasetMetadata;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public interface DatasetStorageService {

    public String storeDataSet(MultipartFile file, DatasetMetadata metadata) throws IOException;
    public MultipartFile getDataSet(String fileUrl) throws RuntimeException;
}
