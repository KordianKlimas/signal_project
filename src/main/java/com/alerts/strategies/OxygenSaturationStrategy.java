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

public class OxygenSaturationStrategy  extends AlertGenerator implements AlertStrategy{

    private DataStorage dataStorage;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public OxygenSaturationStrategy(DataStorage dataStorage) {
        super(dataStorage);
        this.dataStorage = dataStorage;
    }


    @Override
    public List<Alert> checkAlert(Patient patient, long startTime, long endTime) {
        return checkBloodSaturation(patient,startTime,endTime);
    }
    /**
     * if no data within 10-minute window is present, the alert for Rapid Drop alert will not work.
     * The generated  alert  for drop has timeStamp of the first analyzed record where trend drop was found
     * ( the end of 10-minute window).
     * Creates only one alert for one 10-minute window.
     * @param patient - Patient object
     * @param startTime - specifies time window
     * @param endTime - specifies time window
     */
    private List<Alert> checkBloodSaturation(Patient patient, long startTime, long endTime) {
        String patientId = patient.getId();
        List<PatientRecord> saturationRecords = dataStorage.getRecords(patientId, startTime, endTime)
                .stream()
                .filter(record -> "Saturation".equals(record.getRecordType()))
                .sorted(comparingLong(PatientRecord::getTimestamp))
                .collect(Collectors.toList());

        List<Alert> alertsSpotted = new ArrayList<>();
        final double CRITICAL_THRESHOLD_SATURATION = 92;
        final double DROP_THRESHOLD_SATURATION = 5;
        final long WINDOW_TIME_MILLIS = 10 * 60 * 1000; // 10 minutes in milliseconds

        for (int i = 0; i < saturationRecords.size(); i++) {
            PatientRecord currentRecord = saturationRecords.get(i);

            // Check for low saturation alert
            if (currentRecord.getMeasurementValue() < CRITICAL_THRESHOLD_SATURATION) {
                Alert alert = new Alert(patientId, "Low Saturation of oxygen in blood", currentRecord.getTimestamp());
                alertsSpotted.add(alert);
            }

            // Check for rapid drop in saturation within the 10-minute window
            for (int j = i + 1; j < saturationRecords.size(); j++) {
                PatientRecord nextRecord = saturationRecords.get(j);

                // Check if the next record is within the 10-minute window
                if (nextRecord.getTimestamp() - currentRecord.getTimestamp() > WINDOW_TIME_MILLIS) {
                    break;
                }

                double drop = Math.abs(currentRecord.getMeasurementValue() - nextRecord.getMeasurementValue());
                if (drop >= DROP_THRESHOLD_SATURATION) {
                    Alert alert = new Alert(patientId, "Rapid drop of oxygen in blood", nextRecord.getTimestamp());
                    alertsSpotted.add(alert);
                    break; // Alert created, no need to check further within this window
                }
            }
        }
        return alertsSpotted;
    }
}
