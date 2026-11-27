package com.example.leafdoc.service;

import com.example.leafdoc.DTO.prediction.MlPredictResponse;
import com.example.leafdoc.DTO.prediction.PredictionItem;
import com.example.leafdoc.DTO.scan.ScanHistoryResponse;
import com.example.leafdoc.DTO.scan.ScanResponse;
import com.example.leafdoc.client.ImageStorageClient;
import com.example.leafdoc.client.MlServiceClient;
import com.example.leafdoc.entity.Disease;
import com.example.leafdoc.entity.Scan;
import com.example.leafdoc.entity.User;
import com.example.leafdoc.enums.ScanStatus;
import com.example.leafdoc.exceptions.MlServiceResponseException;
import com.example.leafdoc.exceptions.ResourceNotFoundException;
import com.example.leafdoc.repository.DiseaseRepository;
import com.example.leafdoc.repository.PlantRepository;
import com.example.leafdoc.repository.ScanRepository;
import com.example.leafdoc.repository.UserRepository;
import com.example.leafdoc.util.ImageFile;
import com.example.leafdoc.util.ImageValidator;
import com.example.leafdoc.util.StoragePathGenarator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanService {

    private final UserRepository userRepo;
    private final PlantRepository plantRepo;
    private final ScanRepository scanRepo;
    private final DiseaseRepository diseaseRepo;
    private final MlServiceClient mlClient;
    private final ImageStorageClient imageStorageClient;
    private final ImageValidator imageValidator;

    @Transactional
    public ScanResponse createScan(
            MultipartFile file,
            Long uId
    ) {

        User user = userRepo.findById(uId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Authenticated user not found."
                ));

        ImageFile image = new ImageFile(file, imageValidator);  // auto validates the image, if anything goes south throws tantrum(Exception)

        String path = StoragePathGenarator.scanImage(user.getId(),image.getExtension());

        boolean uploaded = false, persisted = false;
        try{
            String publicUrl = imageStorageClient.upload(image, path);
            uploaded = true;
            MlPredictResponse resp = mlClient.predict(image);

            PredictionItem prediction = resp.prediction().stream()
                    .max(Comparator.comparing(PredictionItem::confidenceScore))
                    .orElseThrow(() -> new MlServiceResponseException("Missing prediction."));

            Disease disease = diseaseRepo.findByMlClassLabel(prediction.mlClassLabel()).orElse(null);

            Scan scan = new Scan();
            scan.setUser(user);
            scan.setDisease(disease);
            scan.setPredictedClassLabel(prediction.mlClassLabel());
            scan.setConfidence(prediction.confidenceScore());
            scan.setImagePath(path);
            scan.setTitle(
                    disease != null
                            ? disease.getName()
                            : "Unknown diagnosis"
            );

            if(disease == null)
                scan.setStatus(ScanStatus.UNSUPPORTED);
            else if(prediction.confidenceScore().compareTo(new BigDecimal("0.75")) < 0)
                scan.setStatus(ScanStatus.UNCERTAIN);
            else if(disease.isHealthy())
                scan.setStatus(ScanStatus.HEALTHY);
            else scan.setStatus(ScanStatus.DISEASE_DETECTED);

            scan = scanRepo.saveAndFlush(scan);
            persisted = true;

            return ScanResponse.from(scan, publicUrl);

        } catch (RuntimeException ex) {

            if (uploaded && !persisted) {
                imageStorageClient.delete(path);
            }
            log.error(
                    "Scan processing failed. userId={}, path={}",
                    uId,
                    path,
                    ex
            );
            throw ex;
        }
    }

    // get scan history
    @Transactional(readOnly = true)
    public Page<ScanHistoryResponse> getScanHistory(
            Long userId,
            Pageable pageable
    ) {
        return scanRepo
                .findScanHistory(userId, pageable)
                .map(ScanHistoryResponse::from);
    }

    // get scan details
    @Transactional(readOnly = true)
    public ScanResponse getScanDetails(
            Long scanId,
            Long userId
    ) {
        Scan scan = scanRepo.findScanDetails(scanId, userId)
                .orElseThrow(ResourceNotFoundException::new);

        if (scan.getDisease() != null) {      // initialize the lazy relations
            Hibernate.initialize( scan.getDisease() );
            Hibernate.initialize( scan.getDisease().getCrop() );
        }

        return ScanResponse.from(
                scan,
                imageStorageClient.getPublicImageUrl(scan.getImagePath())
        );

    }

    // scan delete
    @Transactional
    public void deleteScan(Long scanId, Long userId) {

        Scan scan = scanRepo.findScanDetails(scanId, userId)
                .orElseThrow(ResourceNotFoundException::new);

        imageStorageClient.delete(scan.getImagePath());

        scanRepo.delete(scan);
    }
}
