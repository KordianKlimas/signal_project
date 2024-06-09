package com.alerts.strategies;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;

public class ECGStrategy extends AlertGenerator implements AlertStrategy{
    private DataStorage dataStorage;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     */
    public ECGStrategy(DataStorage dataStorage) {
        super(dataStorage);
        this.dataStorage = dataStorage;
    }

    @Override
    public List<Alert> checkAlert(Patient patient, long startTime, long endTime) {
        return checkECG(patient,startTime,endTime);
    }

    /**
     * Creates alert for Abnormal Heart Rate when Heart Rate drops under 50 or is over 100.
     *
     * Creates alert for Irregular beat pattern detected if in the measured data there is record two or more standard deviations.
     * To create  alert for Irregular beat pattern, the standard deviation is calculated for time window of 10 minutes, there has
     * to be at least 5 records within 10 minutes to trigger alert .
     *
     * @param patient - Patient object
     * @param startTime - specifies time window
     * @param endTime - specifies time window
     */
    private List<Alert> checkECG(Patient patient, long startTime, long endTime){
        final int windowSize = 10;
        final int windowTimeMinutes = 10; // in minutes. within this time the standard deviation is calculated
        final double heartRateLowerBound = 50;
        final double heartRateUpperBound = 100;

        String patientId = patient.getId();
        List<PatientRecord> ECGRecords = dataStorage.getRecords(patientId, startTime, endTime)
                .stream()
                .filter(record -> "ECG".equals(record.getRecordType()))
                .sorted(comparingLong(PatientRecord::getTimestamp))
                .collect(Collectors.toList());

        List<Alert> alerts = new ArrayList<>();

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
                    alerts.add(new Alert(patientId, "Irregular Beat Pattern", currentRecord.getTimestamp()));
                }
                previousRecord = currentRecord;
            }
        }


        return alerts;
    }


}
