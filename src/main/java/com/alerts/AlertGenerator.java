package com.alerts;

import com.alerts.decorators.BasicAlert;
import com.alerts.strategies.*;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private final DataStorage dataStorage;
    private final List<BasicAlert> basicAlerts = new LinkedList<>();
    private AlertStrategy alertStrategy;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Sets the strategy for the AlertGenerator.
     *
     * @param alertStrategy the alert strategy to be set
     * @throws NullPointerException if the provided strategy is null
     */
    public void setAlertStrategy(AlertStrategy alertStrategy) {
        Objects.requireNonNull(alertStrategy, "Strategy cannot be null");
        this.alertStrategy = alertStrategy;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        if (patient == null) {
            System.err.println("Null or no patient provided");
            return;
        }
        this.basicAlerts.clear();

        for (PatientRecord record : patient.getAllRecords()) {
            if ("Alert".equals(record.getRecordType()) && record.getMeasurementValue() == 1) {
                basicAlerts.add(new BasicAlert(record.getPatientId(), "Triggered Alert", record.getTimestamp()));
            }
        }

        List<AlertStrategy> strategies = Arrays.asList(
                new BloodPressureStrategy(dataStorage),
                new OxygenSaturationStrategy(dataStorage),
                new ECGStrategy(dataStorage),
                new HeartRateStrategy(dataStorage)
        );

        strategies.forEach(strategy -> basicAlerts.addAll(strategy.checkAlert(patient, 0L, Long.MAX_VALUE)));

        basicAlerts.addAll(checkCompoundAlerts(patient, "SystolicPressure critical value reached", "Low Saturation of oxygen in blood", 0.1, "Hypotensive Hypoxemia Alert"));
        triggerAlerts(basicAlerts);
    }

    /**
     * Checks for compound alerts based on the provided criteria.
     *
     * @param patient           the patient object
     * @param alert1            the first alert condition
     * @param alert2            the second alert condition
     * @param timeInterval      the time interval within which both alerts should occur
     * @param compoundAlertName the name of the compound alert
     * @return a list of compound alerts found
     */
    private List<BasicAlert> checkCompoundAlerts(Patient patient, String alert1, String alert2, double timeInterval, String compoundAlertName) {
        List<BasicAlert> alertsSpotted = new ArrayList<>();
        List<BasicAlert> filteredList = basicAlerts.stream()
                .filter(alert -> alert.getCondition().equals(alert1) || alert.getCondition().equals(alert2))
                .collect(Collectors.toList());

        List<BasicAlert> sortedFilteredBasicAlerts = filteredList.stream()
                .sorted(Comparator.comparing(BasicAlert::getTimestamp))
                .collect(Collectors.toList());

        for (int i = 0; i < sortedFilteredBasicAlerts.size() - 1; i++) {
            long currentTimestamp = sortedFilteredBasicAlerts.get(i).getTimestamp();
            long nextTimestamp = sortedFilteredBasicAlerts.get(i + 1).getTimestamp();

            if (nextTimestamp - currentTimestamp <= timeInterval * 60 * 1000) {
                this.basicAlerts.remove(sortedFilteredBasicAlerts.get(i));
                this.basicAlerts.remove(sortedFilteredBasicAlerts.get(i + 1));

                BasicAlert basicAlert = new BasicAlert(patient.getId(), compoundAlertName, currentTimestamp);
                alertsSpotted.add(basicAlert);
            }
        }
        return alertsSpotted;
    }

    /**
     * Notifies staff about the triggered alerts.
     *
     * @param basicAlerts the list of basic alerts
     */
    private void triggerAlerts(List<BasicAlert> basicAlerts) {
        Map<String, BasicAlert> alertMap = new HashMap<>();

        for (BasicAlert basicAlert : basicAlerts) {
            if (!alertMap.containsKey(basicAlert.getCondition())) {
                alertMap.put(basicAlert.getCondition(), basicAlert);
            }
        }

        for (BasicAlert basicAlert : alertMap.values()) {
            basicAlert.triggerAlert();
        }
    }

    /**
     * Retrieves all non-repeating alerts.
     *
     * @return a list of non-repeating alerts
     */
    public List<BasicAlert> getAlerts() {
        Map<String, BasicAlert> alertMap = new HashMap<>();

        for (BasicAlert basicAlert : basicAlerts) {
            if (!alertMap.containsKey(basicAlert.getCondition())) {
                alertMap.put(basicAlert.getCondition(), basicAlert);
            }
        }

        return new LinkedList<>(alertMap.values());
    }

    /**
     * Retrieves all non-repeating alerts for testing purposes.
     *
     * @return a list of non-repeating alerts
     */
    public List<BasicAlert> getAlerts_Junit() {
        Map<String, BasicAlert> alertMap = new HashMap<>();
        for (BasicAlert basicAlert : basicAlerts) {
            if (!alertMap.containsKey(basicAlert.getCondition())) {
                alertMap.put(basicAlert.getCondition(), basicAlert);
            }
        }

        return new LinkedList<>(alertMap.values());
    }
}