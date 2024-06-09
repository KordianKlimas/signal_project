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

public class HeartRateStrategy extends AlertGenerator implements AlertStrategy{

    private DataStorage dataStorage;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public HeartRateStrategy(DataStorage dataStorage) {
        super(dataStorage);
        this.dataStorage = dataStorage;
    }


    @Override
    public List<Alert> checkAlert(Patient patient, long startTime, long endTime) {
        return checkHeartRate(patient,startTime,endTime);
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
    private List<Alert> checkHeartRate(Patient patient, long startTime, long endTime){
        final int windowSize = 10;
        final double heartRateLowerBound = 50;
        final double heartRateUpperBound = 100;

        String patientId = patient.getId();
        List<PatientRecord> ECGRecords = dataStorage.getRecords(patientId, startTime, endTime)
                .stream()
                .filter(record -> "ECG".equals(record.getRecordType()))
                .sorted(comparingLong(PatientRecord::getTimestamp))
                .collect(Collectors.toList());

        List<Alert> alerts = new ArrayList<>();

        // Abnormal Heart Rate Detection
        int windowStart = 0;
        for (int i = 0; i < ECGRecords.size(); i++) {
            // Check if window is filled
            if (i >= windowSize - 1) {
                // Calculate BPM within the window
                double bpm = calculateBPM(ECGRecords.subList(windowStart, i + 1));

                // Check for abnormal heart rate
                if (bpm < heartRateLowerBound || bpm > heartRateUpperBound) {
                    PatientRecord record = ECGRecords.get(i);
                    Alert alert = new Alert(patient.getId(), "Abnormal Heart Rate", record.getTimestamp());
                    alerts.add(alert);
                }
                // Move the window by one data point
                windowStart++;
            }
        }

        return alerts;
    }

    /**
     * Calculates BPM for patient in specified time window
     * @param ecgData
     * @return
     */
    public  double calculateBPM(List<PatientRecord> ecgData) {
        if (ecgData.isEmpty()) {
            return 0;
        }
        long startTime = ecgData.get(0).getTimestamp();
        long endTime = ecgData.get(ecgData.size() - 1).getTimestamp();
        double durationMinutes = (endTime - startTime) / (1000.0 * 60.0);
        if (durationMinutes == 0) {
            return 0; // Prevent division by zero
        }
        return (ecgData.size() / durationMinutes); // returns bpm
    }
}
