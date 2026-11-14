package com.example.leafdoc.service;

import com.example.leafdoc.DTO.catalog.CropCatalogEntry;
import com.example.leafdoc.DTO.catalog.DiseaseCatalogDocument;
import com.example.leafdoc.DTO.catalog.DiseaseCatalogEntry;
import com.example.leafdoc.entity.Crop;
import com.example.leafdoc.entity.Disease;
import com.example.leafdoc.repository.CropRepository;
import com.example.leafdoc.repository.DiseaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiseaseCatalogImportService {
    private static final String CATALOG_PATH =
            "catalog/disease-catalog.json";

    private final ObjectMapper objectMapper;
    private final CropRepository cropRepository;
    private final DiseaseRepository diseaseRepository;

    @Transactional
    public void importCatalog(boolean updateExisting) {
    
        DiseaseCatalogDocument catalog = readCatalog();
        Map<String, Crop> cropMap = importCrops(
                catalog.crops(), updateExisting
        );

        importDiseases(updateExisting, catalog, cropMap);
        log.debug("Disease catalog import completed. Version: {}", catalog.catalogVersion());
    }


    private DiseaseCatalogDocument readCatalog() {

        try (InputStream input = new ClassPathResource(
                CATALOG_PATH
        ).getInputStream()) {

            DiseaseCatalogDocument catalog =  objectMapper.readValue(
                    input,
                    DiseaseCatalogDocument.class
            );
            validateCatalog(catalog);
            return catalog;
        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Unable to read disease catalog: " + CATALOG_PATH,
                    ex
            );
        }
    }

    private Map<String, Crop> importCrops(
            List<CropCatalogEntry> entries,
            boolean updateExisting
    ) {

        Map<String, Crop> result = new HashMap<>();

        for (CropCatalogEntry entry : entries) {
            Optional<Crop> existing =
                    cropRepository.findByCropCode(entry.code());
            Crop crop;
            if (existing.isPresent()) {
                crop = existing.get();
                if (!Objects.equals(crop.getName(), entry.name())) {
                    if (!updateExisting) {
                        throw new IllegalStateException(
                                "Existing crop name differs from catalog: "
                                        + entry.code()
                        );
                    }
                    crop.setName(entry.name());
                    cropRepository.save(crop);
                }
            } else {
                crop = new Crop();
                crop.setCropCode(entry.code());
                crop.setName(entry.name());
                crop = cropRepository.save(crop);
            }
            result.put(entry.code(), crop);
        }
        return result;
    }
    private void importDiseases(
            boolean updateExisting,
            DiseaseCatalogDocument catalog,
            Map<String, Crop> cropMap
    ) {
        int inserted = 0;
        int updated = 0;
        int unchanged = 0;

        for (DiseaseCatalogEntry entry : catalog.diseases()) {
            Crop crop = cropMap.get(entry.cropCode());
            Optional<Disease> existing =
                    diseaseRepository.findByMlClassLabel(
                            entry.mlClassLabel()
                    );
            if (existing.isPresent()) {
                Disease disease = existing.get();
                if (isSameContent(disease, entry, crop)) {
                    unchanged++;
                    continue;
                }
                if (!updateExisting) {
                    throw new IllegalStateException(
                            "Existing disease differs from catalog: "
                                    + entry.mlClassLabel()
                                    + ". Run with update mode to modify it."
                    );
                }
                updateDisease(disease, entry, crop);
                diseaseRepository.save(disease);
                updated++;
            } else {
                Disease disease = new Disease();
                updateDisease(disease, entry, crop);
                diseaseRepository.save(disease);
                inserted++;
            }
        }
        log.debug("Diseases inserted: {}, updated: {}, unchanged: {}", inserted, updated, unchanged);
    }

    private void updateDisease(
            Disease disease,
            DiseaseCatalogEntry entry,
            Crop crop
    ) {
        disease.setCrop(crop);
        disease.setMlClassLabel(entry.mlClassLabel());
        disease.setName(entry.name());
        disease.setDescription(entry.description());
        disease.setSymptoms(entry.symptoms());
        disease.setCauses(entry.causes());
        disease.setTreatment(entry.treatment());
        disease.setPrevention(entry.prevention());
        disease.setCareTips(entry.careTips());
        disease.setHealthy(entry.isHealthy());
    }

    private boolean isSameContent(
            Disease existing,
            DiseaseCatalogEntry entry,
            Crop crop
    ) {

        return Objects.equals(
                existing.getCrop().getId(), crop.getId()
        )
                && Objects.equals(
                existing.getName(), entry.name()
        )
                && Objects.equals(
                existing.getDescription(), entry.description()
        )
                && Objects.equals(
                existing.getSymptoms(), entry.symptoms()
        )
                && Objects.equals(
                existing.getCauses(), entry.causes()
        )
                && Objects.equals(
                existing.getTreatment(), entry.treatment()
        )
                && Objects.equals(
                existing.getPrevention(), entry.prevention()
        )
                && Objects.equals(
                existing.getCareTips(), entry.careTips()
        )
                && existing.isHealthy() == entry.isHealthy();
    }

    private void validateCatalog(
            DiseaseCatalogDocument catalog
    ) {

        if (catalog == null
                || catalog.catalogVersion() == null
                || catalog.catalogVersion().isBlank()
                || catalog.crops() == null
                || catalog.diseases() == null
                || catalog.crops().isEmpty()
                || catalog.diseases().isEmpty()) {

            throw new IllegalStateException(
                    "Catalog is missing required data."
            );
        }

        var cropCodes = getCropCodes(catalog);

        Set<String> labels = new HashSet<>();

        for (DiseaseCatalogEntry disease : catalog.diseases()) {

            if (disease.cropCode() == null
                    || !cropCodes.contains(disease.cropCode())) {
                throw new IllegalStateException(
                        "Unknown crop code for ML label: "
                                + disease.mlClassLabel()
                );
            }

            if (disease.mlClassLabel() == null
                    || disease.mlClassLabel().isBlank()
                    || disease.mlClassLabel().length() > 150) {
                throw new IllegalStateException(
                        "Invalid ML class label."
                );
            }

            if (!labels.add(disease.mlClassLabel())) {
                throw new IllegalStateException(
                        "Duplicate ML class label: "
                                + disease.mlClassLabel()
                );
            }

            if (disease.name() == null || disease.name().isBlank()) {
                throw new IllegalStateException(
                        "Missing disease name for "
                                + disease.mlClassLabel()
                );
            }

            if (disease.description() == null
                    || disease.description().isBlank()
                    || disease.symptoms() == null
                    || disease.causes() == null
                    || disease.treatment() == null
                    || disease.prevention() == null
                    || disease.careTips() == null) {
                throw new IllegalStateException(
                        "Missing disease information for "
                                + disease.mlClassLabel()
                );
            }
        }
    }

    private static @NonNull Set<String> getCropCodes(
            DiseaseCatalogDocument catalog
    ) {
        Set<String> cropCodes = new HashSet<>();

        for (CropCatalogEntry crop : catalog.crops()) {

            if (crop.code() == null || crop.code().isBlank()
                    || crop.name() == null || crop.name().isBlank()) {
                throw new IllegalStateException(
                        "Invalid crop entry."
                );
            }

            if (!cropCodes.add(crop.code())) {
                throw new IllegalStateException(
                        "Duplicate crop code: " + crop.code()
                );
            }
        }
        return cropCodes;
    }
}
