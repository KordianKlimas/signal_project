package com.alerts.strategies;

import com.alerts.decorators.BasicAlert;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;

/**
 * A strategy for monitoring ECG data to detect abnormalities.
 */
public class ECGStrategy implements AlertStrategy {
    private final DataStorage dataStorage;

    /**
     * Constructs an ECGStrategy object with a specified DataStorage.
     * The DataStorage is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient data
     */
    public ECGStrategy(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Checks for ECG alerts for the given patient within the specified time range.
     * Alerts are generated for abnormal heart rate and irregular beat patterns.
     *
     * @param patient   the patient for whom ECG alerts are checked
     * @param startTime the start time of the time window
     * @param endTime   the end time of the time window
     * @return a list of BasicAlert objects representing the ECG alerts found
     */
    @Override
    public List<BasicAlert> checkAlert(Patient patient, long startTime, long endTime) {
        return checkECG(patient, startTime, endTime);
    }

    /**
     * Checks ECG data for abnormalities and generates alerts accordingly.
     *
     * @param patient   the patient whose ECG data is being analyzed
     * @param startTime the start time of the time window
     * @param endTime   the end time of the time window
     * @return a list of BasicAlert objects representing the ECG alerts found
     */
    private List<BasicAlert> checkECG(Patient patient, long startTime, long endTime) {
        String patientId = patient.getId();
        List<PatientRecord> ECGRecords = dataStorage.getRecords(patientId, startTime, endTime)
                .stream()
                .filter(record -> "ECG".equals(record.getRecordType()))
                .sorted(comparingLong(PatientRecord::getTimestamp))
                .collect(Collectors.toList());

        List<BasicAlert> basicAlerts = new ArrayList<>();

        // Irregular Beat Pattern Detection
        if (ECGRecords.size() > 1) {
            long totalInterval = 0;
            for (int i = 1; i < ECGRecords.size(); i++) {
                totalInterval += (ECGRecords.get(i).getTimestamp() - ECGRecords.get(i - 1).getTimestamp());
            }
            double averageInterval = totalInterval / (double) (ECGRecords.size() - 1);
            double allowableVariation = averageInterval * 0.1; // Allowing 10% variation

            PatientRecord previousRecord = ECGRecords.get(0);
            for (int i = 1; i < ECGRecords.size(); i++) {
                PatientRecord currentRecord = ECGRecords.get(i);
                long intervalDifference = Math.abs(currentRecord.getTimestamp() - previousRecord.getTimestamp());

                if (Math.abs(intervalDifference - averageInterval) > allowableVariation) {
                    basicAlerts.add(new BasicAlert(patientId, "Irregular Beat Pattern", currentRecord.getTimestamp()));
                }
                previousRecord = currentRecord;
            }
        }

        return basicAlerts;
    }
}
