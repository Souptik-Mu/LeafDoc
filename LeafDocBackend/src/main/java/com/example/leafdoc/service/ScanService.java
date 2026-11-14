package com.example.leafdoc.service;

import com.example.leafdoc.DTO.scan.DiseaseInfo;
import com.example.leafdoc.DTO.prediction.MlPredictResponse;
import com.example.leafdoc.DTO.prediction.PredictionItem;
import com.example.leafdoc.DTO.scan.ScanResponse;
import com.example.leafdoc.client.ImageStorageClient;
import com.example.leafdoc.client.MlServiceClient;
import com.example.leafdoc.entity.Disease;
import com.example.leafdoc.entity.Plant;
import com.example.leafdoc.entity.Scan;
import com.example.leafdoc.entity.User;
import com.example.leafdoc.enums.ScanStatus;
import com.example.leafdoc.repository.DiseaseRepository;
import com.example.leafdoc.repository.PlantRepository;
import com.example.leafdoc.repository.ScanRepository;
import com.example.leafdoc.repository.UserRepository;
import com.example.leafdoc.util.ImageFile;
import com.example.leafdoc.util.StoragePathGenarator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScanService {

    private final UserRepository userRepo;
    private final PlantRepository plantRepo;
    private final ScanRepository scanRepo;
    private final DiseaseRepository diseaseRepo;
    private final MlServiceClient mlClient;
    private final ImageStorageClient imageStorageClient;

    @Transactional
    public ScanResponse createScan(
            MultipartFile file,
            Long plantId,
            Long uId
    ) {

        User user = userRepo.findById(uId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Authenticated user not found."
                ));

        Plant plant = plantRepo.findByIdAndUserId(plantId, user.getId()).orElse(null);

        ImageFile image = new ImageFile(file);  // auto validates the image, if anything goes south throws tantrum(Exception)

        String path = StoragePathGenarator.scanImage(user.getId(),image.getExtension());

        boolean uploaded = false, persisted = false;
        try{
            String publicUrl = imageStorageClient.upload(image, path);
            uploaded = true;
            MlPredictResponse resp = mlClient.predict(image);
            List<PredictionItem> sortedPredictions =
                    resp.prediction().stream()
//                        .filter(p -> p.confidenceScore()
//                                .compareTo(new BigDecimal("0.75")) > 0
//                        )
                            .sorted(Comparator.comparing(
                                    PredictionItem::confidenceScore
                            ).reversed())
                            .toList();

            PredictionItem prediction =  sortedPredictions.getFirst();//temp ; also get status

            Disease disease = diseaseRepo.findByMlClassLabel(prediction.mlClassLabel()).orElse(null);

            Scan scan = new Scan();
            scan.setUser(user);
            scan.setPredictedClassLabel(prediction.mlClassLabel());
            scan.setConfidence(prediction.confidenceScore());
            scan.setImagePath(path);
            if(disease == null)
                scan.setStatus(ScanStatus.UNSUPPORTED);
            else if(prediction.confidenceScore().compareTo(new BigDecimal("0.75")) < 0)
                scan.setStatus(ScanStatus.UNCERTAIN);
            else if(disease.isHealthy())
                scan.setStatus(ScanStatus.HEALTHY);
            else scan.setStatus(ScanStatus.DISEASE_DETECTED);

            scan = scanRepo.save(scan);
            persisted = true;

            // return the public image url, desease info, scan status
            return new ScanResponse(
                    disease.getCrop().getName(),
                    DiseaseInfo.from(disease),
                    scan.getStatus(),
                    disease.isHealthy(),
                    publicUrl
            );


        } catch (RuntimeException ex) {

            if (uploaded && !persisted) {
                imageStorageClient.delete(path);
            }

            throw ex;
        }
    }
    // get scan history
    // get scan details
    // scan delete
}
