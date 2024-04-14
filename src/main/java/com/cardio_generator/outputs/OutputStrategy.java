package com.cardio_generator.outputs;

/**
 * Interface defining strategy of output, used in server data transfer
 */
public interface OutputStrategy {
    /**
     * Outputs data related to a patient.
     *
     * @param patientId The ID of the patient.
     * @param timestamp The timestamp of the data.
     * @param label     The label associated with the data.
     * @param data      The actual data to be output.
     */
    void output(int patientId, long timestamp, String label, String data);
}
