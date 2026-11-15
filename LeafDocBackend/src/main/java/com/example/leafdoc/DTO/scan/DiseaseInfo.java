package com.example.leafdoc.DTO.scan;

import com.example.leafdoc.entity.Disease;

import java.util.List;

public record DiseaseInfo(
        //String cropName,
        String name,
        String description,
        List<String> symptoms,
        List<String> causes,
        List<String> treatment,
        List<String> prevention,
        List<String> careTips
        //boolean healthy
) {

    public static DiseaseInfo from(Disease disease) {

        return new DiseaseInfo(
                //disease.getCrop().getName(),
                disease.getName(),
                disease.getDescription(),
                disease.getSymptoms(),
                disease.getCauses(),
                disease.getTreatment(),
                disease.getPrevention(),
                disease.getCareTips()
                //disease.isHealthy()
        );
    }
}