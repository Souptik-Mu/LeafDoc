package com.example.leafdoc.util;

import com.example.leafdoc.service.DiseaseCatalogImportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "leafdoc.catalog.import",
        havingValue = "true"
)
public class DiseaseCatalogImportRunner implements ApplicationRunner {

    private final DiseaseCatalogImportService importService;
    private final boolean updateExisting;

    public DiseaseCatalogImportRunner(
            DiseaseCatalogImportService importService,
            @Value("${leafdoc.catalog.update-existing:false}")
            boolean updateExisting
    ) {
        this.importService = importService;
        this.updateExisting = updateExisting;
    }

    @Override
    public void run(ApplicationArguments args) {
        importService.importCatalog(updateExisting);
    }
}
