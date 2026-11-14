package com.example.leafdoc.DTO;

import com.example.leafdoc.entity.Disease;

import java.util.List;

public record DiseaseInfoResponse(
        String cropName,
        String mlClassLabel,
        String name,
        String description,
        List<String> symptoms,
        List<String> causes,
        List<String> treatment,
        List<String> prevention,
        List<String> careTips,
        boolean healthy
) {

    public static DiseaseInfoResponse from(Disease disease) {

        return new DiseaseInfoResponse(
                disease.getCrop().getName(),
                disease.getMlClassLabel(),
                disease.getName(),
                disease.getDescription(),
                disease.getSymptoms(),
                disease.getCauses(),
                disease.getTreatment(),
                disease.getPrevention(),
                disease.getCareTips(),
                disease.isHealthy()
        );
    }
}