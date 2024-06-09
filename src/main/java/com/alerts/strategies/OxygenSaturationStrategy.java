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
 * An alert strategy for monitoring oxygen saturation levels.
 */
public class OxygenSaturationStrategy extends AlertGenerator implements AlertStrategy {

    private final DataStorage dataStorage;

    /**
     * Constructs an OxygenSaturationStrategy with the specified data storage.
     *
     * @param dataStorage the data storage system that provides access to patient data
     */
    public OxygenSaturationStrategy(DataStorage dataStorage) {
        super(dataStorage);
        this.dataStorage = dataStorage;
    }

    /**
     * Checks for low oxygen saturation levels and rapid drops in saturation for the given patient within the specified time frame.
     *
     * @param patient   the patient object
     * @param startTime the start time of the evaluation period
     * @param endTime   the end time of the evaluation period
     * @return a list of basic alerts indicating any detected abnormalities
     */
    @Override
    public List<BasicAlert> checkAlert(Patient patient, long startTime, long endTime) {
        return checkBloodSaturation(patient, startTime, endTime);
    }

    /**
     * Checks for low oxygen saturation levels and rapid drops in saturation for the given patient within the specified time frame.
     *
     * @param patient   the patient object
     * @param startTime the start time of the evaluation period
     * @param endTime   the end time of the evaluation period
     * @return a list of basic alerts indicating any detected abnormalities
     */
    private List<BasicAlert> checkBloodSaturation(Patient patient, long startTime, long endTime) {
        String patientId = patient.getId();
        List<PatientRecord> saturationRecords = dataStorage.getRecords(patientId, startTime, endTime)
                .stream()
                .filter(record -> "Saturation".equals(record.getRecordType()))
                .sorted(comparingLong(PatientRecord::getTimestamp))
                .collect(Collectors.toList());

        List<BasicAlert> alertsSpotted = new ArrayList<>();
        final double CRITICAL_THRESHOLD_SATURATION = 92;
        final double DROP_THRESHOLD_SATURATION = 5;
        final long WINDOW_TIME_MILLIS = 10 * 60 * 1000; // 10 minutes in milliseconds

        for (int i = 0; i < saturationRecords.size(); i++) {
            PatientRecord currentRecord = saturationRecords.get(i);

            // Check for low saturation alert
            if (currentRecord.getMeasurementValue() < CRITICAL_THRESHOLD_SATURATION) {
                BasicAlert basicAlert = new BasicAlert(patientId, "Low Saturation of oxygen in blood", currentRecord.getTimestamp());
                alertsSpotted.add(basicAlert);
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
                    BasicAlert basicAlert = new BasicAlert(patientId, "Rapid drop of oxygen in blood", nextRecord.getTimestamp());
                    alertsSpotted.add(basicAlert);
                    break; // Alert created, no need to check further within this window
                }
            }
        }
        return alertsSpotted;
    }
}
