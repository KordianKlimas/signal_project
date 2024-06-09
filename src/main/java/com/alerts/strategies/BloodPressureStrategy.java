package com.alerts.strategies;

import com.alerts.decorators.BasicAlert;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;

/**
 * An alert strategy for monitoring blood pressure.
 */
public class BloodPressureStrategy implements AlertStrategy {
    private final DataStorage dataStorage;

    /**
     * Constructs a BloodPressureStrategy object with a specified DataStorage.
     *
     * @param dataStorage the data storage system that provides access to patient data
     */
    public BloodPressureStrategy(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Checks for blood pressure alerts within a specified time range for a given patient.
     *
     * @param patient   The patient for whom blood pressure alerts are checked.
     * @param startTime The start time of the time range.
     * @param endTime   The end time of the time range.
     * @return A list of BasicAlert objects representing the blood pressure alerts found.
     */
    @Override
    public List<BasicAlert> checkAlert(Patient patient, long startTime, long endTime) {
        return checkBloodPressure(patient, startTime, endTime);
    }
    /**
     * Checks for blood pressure alerts for the given patient within the specified time range.
     *
     * @param patient   The patient for whom blood pressure alerts are checked.
     * @param startTime The start time of the time range.
     * @param endTime   The end time of the time range.
     * @return A list of BasicAlert objects representing the blood pressure alerts found.
     */
    private List<BasicAlert> checkBloodPressure(Patient patient, long startTime, long endTime) {
        String patientId = patient.getId();

        // Get systolic and diastolic records within the specified time range
        List<PatientRecord> systolicRecords = dataStorage.getRecords(patientId, startTime, endTime)
                .parallelStream()
                .filter(record -> "SystolicPressure".equals(record.getRecordType()))
                .sorted(comparingLong(PatientRecord::getTimestamp).reversed())
                .collect(toList());

        List<PatientRecord> diastolicRecords = dataStorage.getRecords(patientId, startTime, endTime)
                .parallelStream()
                .filter(record -> "DiastolicPressure".equals(record.getRecordType()))
                .sorted(comparingLong(PatientRecord::getTimestamp).reversed())
                .collect(toList());

        // Variables for threshold values and trend detection
        final double TREND_THRESHOLD = 10;
        final int TREND_READINGS = 3;
        final double SYSTOLIC_CRITICAL_MIN = 90;
        final double SYSTOLIC_CRITICAL_MAX = 180;
        final double DIASTOLIC_CRITICAL_MIN = 60;
        final double DIASTOLIC_CRITICAL_MAX = 120;

        List<BasicAlert> alerts = new ArrayList<>();

        // Check for trends and critical values in systolic records
        checkAlertsForBloodPressure(patientId, systolicRecords, SYSTOLIC_CRITICAL_MIN, SYSTOLIC_CRITICAL_MAX, TREND_THRESHOLD, TREND_READINGS, alerts);

        // Check for trends and critical values in diastolic records
        checkAlertsForBloodPressure(patientId, diastolicRecords, DIASTOLIC_CRITICAL_MIN, DIASTOLIC_CRITICAL_MAX, TREND_THRESHOLD, TREND_READINGS, alerts);

        return alerts;
    }
    /**
     * Checks for blood pressure alerts based on the given records and thresholds.
     *
     * @param patientId     The ID of the patient for whom the alerts are checked.
     * @param records       The list of patient records to analyze.
     * @param criticalMin   The minimum critical threshold value.
     * @param criticalMax   The maximum critical threshold value.
     * @param trendThreshold   The threshold for detecting trends.
     * @param trendReadings The number of consecutive readings considered for trend detection.
     * @param alerts        The list to which generated alerts are added.
     */
    private void checkAlertsForBloodPressure(String patientId, List<PatientRecord> records, double criticalMin, double criticalMax, double trendThreshold, int trendReadings, List<BasicAlert> alerts) {
        if (!records.isEmpty()) {
            int trendCounter = 0;
            double changeTemp = 0;

            for (int i = 0; i < records.size() - 1; i++) {
                double pressureValCurrent = records.get(i).getMeasurementValue();
                double pressureValNext = records.get(i + 1).getMeasurementValue();
                double change = pressureValCurrent - pressureValNext;

                boolean trendAlternated = changeTemp * change < 0;
                changeTemp = change;

                if (Math.abs(change) > trendThreshold && !trendAlternated) {
                    trendCounter++;
                } else {
                    trendCounter = 0;
                }

                if (trendCounter == trendReadings - 1) {
                    BasicAlert basicAlert = new BasicAlert(patientId, records.get(i - trendReadings + 2).getRecordType() + " dangerous trend", records.get(i - trendReadings + 2).getTimestamp());
                    alerts.add(basicAlert);
                }

                if (criticalMax < pressureValCurrent || criticalMin > pressureValCurrent) {
                    BasicAlert basicAlert = new BasicAlert(patientId, records.get(i).getRecordType() + " critical value reached", records.get(i).getTimestamp());
                    alerts.add(basicAlert);
                }

                if (i < records.size() - 1 && (criticalMax < pressureValNext || criticalMin > pressureValNext)) {
                    BasicAlert basicAlert = new BasicAlert(patientId, records.get(i + 1).getRecordType() + " critical value reached", records.get(i + 1).getTimestamp());
                    alerts.add(basicAlert);
                }
            }

            if (records.size() == 1) {
                double pressureValCurrent = records.get(0).getMeasurementValue();
                if (criticalMax < pressureValCurrent || criticalMin > pressureValCurrent) {
                    BasicAlert basicAlert = new BasicAlert(patientId, records.get(0).getRecordType() + " critical value reached", records.get(0).getTimestamp());
                    alerts.add(basicAlert);
                }
            }
        }
    }
}
