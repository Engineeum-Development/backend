package genum.dataset.service;

import genum.dataset.domain.DatasetMetadata;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudStorageService implements DatasetStorageService{
    @Override
    public String storeDataSet(MultipartFile file, DatasetMetadata metadata) {
        return null;
    }

    @Override
    public MultipartFile getDataSet(String fileUrl) throws RuntimeException {
        return null;
    }
}
