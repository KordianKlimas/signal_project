package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Interface for generating artificial patient data for chosen patient, with defined output strategy.
 *
 */
public interface PatientDataGenerator {
    /**
     * method generating artificial patient data for chosen patient, using chosen output strategy.
     *
     * @param patientId The ID of the patient.
     * @param outputStrategy OutputStrategy  (interface)
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
