package genum.dataset.service;

import genum.dataset.domain.DatasetMetadata;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface DatasetStorageService {

    String storeDataSet(MultipartFile file, DatasetMetadata metadata) throws IOException;
    String deleteDataset(String id) throws IOException;

}
