package genum.dataset.config;

import genum.dataset.service.DatasetsService;
import genum.dataset.service.DatasetsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasetConfiguration {

    @Bean
    public DatasetsService datasetsService() {
        return new DatasetsServiceImpl();
    }
}
