package com.example.leafdoc.util;

import org.springframework.boot.CommandLineRunner;

public class DiseaseCatalogImporter implements CommandLineRunner {
    // imports catalog/delesse-catalog.json to DB table (SEEDING)
    @Override
    public void run(String... args) throws Exception {

        // Only seed if the table is empty
        if (diseaseRepository.count() > 0) {
            return;
        }
        // read json & map to a object
        //InputStream input = new ClassPathResource("catalog/disease-catalog.json").getInputStream();

        /// the object holds the xerox of the json in object form (DTO)
        ///

        // make bulk update to db.

    }
}
