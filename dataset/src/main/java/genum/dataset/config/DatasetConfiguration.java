package genum.dataset.config;

import com.cloudinary.Cloudinary;
import genum.dataset.repository.DatasetRepository;
import genum.dataset.service.DatasetStorageService;
import genum.dataset.service.DatasetsService;
import genum.dataset.service.DatasetsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasetConfiguration {
    @Value("${cloudinary.url}")
    private String cloudinaryURL;

    @Bean
    public DatasetsService datasetsService(DatasetRepository datasetRepository, DatasetStorageService datasetStorageService) {
        return new DatasetsServiceImpl(datasetRepository, datasetStorageService);
    }
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(cloudinaryURL);
    }
}
