package com.alerts.strategies;

import com.alerts.decorators.BasicAlert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;

/**
 * An alert strategy for monitoring heart rate abnormalities.
 */
public class HeartRateStrategy extends AlertGenerator implements AlertStrategy {

    private final DataStorage dataStorage;

    /**
     * Constructs a HeartRateStrategy with the specified data storage.
     *
     * @param dataStorage the data storage system that provides access to patient data
     */
    public HeartRateStrategy(DataStorage dataStorage) {
        super(dataStorage);
        this.dataStorage = dataStorage;
    }

    /**
     * Checks for abnormal heart rate and irregular beat patterns for the given patient within the specified time frame.
     *
     * @param patient    the patient object
     * @param startTime  the start time of the evaluation period
     * @param endTime    the end time of the evaluation period
     * @return a list of basic alerts indicating any detected abnormalities
     */
    @Override
    public List<BasicAlert> checkAlert(Patient patient, long startTime, long endTime) {
        return checkHeartRate(patient, startTime, endTime);
    }

    /**
     * Checks for abnormal heart rate and irregular beat patterns for the given patient within the specified time frame.
     *
     * @param patient    the patient object
     * @param startTime  the start time of the evaluation period
     * @param endTime    the end time of the evaluation period
     * @return a list of basic alerts indicating any detected abnormalities
     */
    private List<BasicAlert> checkHeartRate(Patient patient, long startTime, long endTime) {
        final int windowSize = 10;
        final double heartRateLowerBound = 50;
        final double heartRateUpperBound = 100;

        String patientId = patient.getId();
        List<PatientRecord> ECGRecords = dataStorage.getRecords(patientId, startTime, endTime)
                .stream()
                .filter(record -> "ECG".equals(record.getRecordType()))
                .sorted(comparingLong(PatientRecord::getTimestamp))
                .collect(Collectors.toList());

        List<BasicAlert> basicAlerts = new ArrayList<>();

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
                    BasicAlert basicAlert = new BasicAlert(patient.getId(), "Abnormal Heart Rate", record.getTimestamp());
                    basicAlerts.add(basicAlert);
                }
                // Move the window by one data point
                windowStart++;
            }
        }

        return basicAlerts;
    }

    /**
     * Calculates the beats per minute (BPM) for the given ECG data.
     *
     * @param ecgData the ECG data records
     * @return the BPM calculated from the ECG data
     */
    public double calculateBPM(List<PatientRecord> ecgData) {
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
