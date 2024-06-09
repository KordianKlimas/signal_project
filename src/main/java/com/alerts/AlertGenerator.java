package com.alerts;

import com.alerts.decorators.Alert;
import com.alerts.decorators.BasicAlert;
import com.alerts.strategies.*;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    public List<BasicAlert> basicAlerts = new LinkedList<>();
    public AlertStrategy alertStrategy;
    public Alert alertStartegy;

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
     * Sets strategy for the AlertGenerator
     *
     * @param alertStrategy - AlertStrategy interface
     */
    public void setAlertStrategy(AlertStrategy alertStrategy) {
        if (alertStrategy == null) {
            throw new NullPointerException("Strategy cannot be null");
        }
        this.alertStrategy = alertStrategy;
    }
    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlerts}
     * method. This method should define the specific conditions under which an
     * alert will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        // the alert definition  methods  could be replaced with different more scalable approach that would allow to create new types of
        // record types beside the 7 that are currently supported ( Cholesterol, ECG.....) and
        // check compound alerts created by staff

        if (patient == null) {
            System.err.println("Null or no patient provided");
            return;
        }
        this.basicAlerts =new LinkedList<>();

        for(PatientRecord record:patient.getAllRecords()){
            if(record.getRecordType().equals("Alert")&&record.getMeasurementValue()==1){
                basicAlerts.add(new BasicAlert(record.getPatientId(),"Triggered Alert",record.getTimestamp()));
            }
        }

        // trigger alert if certain condition are met
        setAlertStrategy(new BloodPressureStrategy(dataStorage));
        this.basicAlerts.addAll(alertStrategy.checkAlert(patient,0L,9223372036854775807L));

        setAlertStrategy(new OxygenSaturationStrategy(dataStorage));
        this.basicAlerts.addAll(alertStrategy.checkAlert(patient,0L,9223372036854775807L));

        setAlertStrategy(new ECGStrategy(dataStorage));
        this.basicAlerts.addAll(alertStrategy.checkAlert(patient,0L,9223372036854775807L));

        setAlertStrategy(new HeartRateStrategy(dataStorage));
        this.basicAlerts.addAll(alertStrategy.checkAlert(patient,0L,9223372036854775807L));

        // compound alert:
        this.basicAlerts.addAll(checkCompoundAlerts(patient,"SystolicPressure critical value reached","Low Saturation of oxygen in blood",0.1,"Hypotensive Hypoxemia Alert"));
        triggerAlerts(basicAlerts);
    }

    /**
     * Allows to evaluate patient's data in specyfied time window or for whole patient's data.
     * @param patient - Patient object
     * @param startTime - specifies time window
     * @param endTime - specifies time window
     */
    public void evaluateData_StrategyPattern(Patient patient ,long startTime, long endTime) {

        if (patient == null) {
            throw new NullPointerException("Patient cannot be null.");
        } else if (alertStrategy == null) {
            throw new NullPointerException("Strategy cannot be null.");
        }

        for(PatientRecord record:patient.getAllRecords()){
            if(record.getRecordType().equals("Alert")&&record.getMeasurementValue()==1){
                basicAlerts.add(new BasicAlert(record.getPatientId(),"Triggered Alert",record.getTimestamp()));
            }
        }

        alertStrategy.checkAlert(patient, startTime,endTime);
    }

    /**
     *  Creates compound alert  based on timeInterval, and given alerts
     * @param patient patient object
     * @param alert1 - alert that has to be triggered
     * @param alert2 - alert that has to be triggered
     * @param timeInterval - time in which  both alerts have to be triggered
     * @param compoundAlertName - new composed alert
     * @return List<Alert>
     */
    private List<BasicAlert> checkCompoundAlerts(Patient patient, String alert1, String alert2, double timeInterval, String compoundAlertName){
        // List to store the alerts that meet certain conditions
        List<BasicAlert> alertsSpotted = new ArrayList<>();

        // Filtering alerts based on specific conditions
        List<BasicAlert> filteredList = basicAlerts.stream()
                .filter(alert -> alert.getCondition().equals(alert1) || alert.getCondition().equals(alert2))
                .collect(toList());

        // Sorting the filtered alerts by timestamp
        List<BasicAlert> sortedFilteredBasicAlerts = filteredList.stream()
                .sorted(Comparator.comparing(BasicAlert::getTimestamp))
                .collect(toList());

        // Iterating through the sorted filtered alerts to find consecutive alerts within a time interval
        for (int i = 0; i < sortedFilteredBasicAlerts.size() - 1; i++) {
            long currentTimestamp = sortedFilteredBasicAlerts.get(i).getTimestamp();
            long nextTimestamp = sortedFilteredBasicAlerts.get(i + 1).getTimestamp();

            // Checking if the time difference between consecutive alerts is within the specified time interval
            if (nextTimestamp - currentTimestamp <= timeInterval * 60 * 1000) {
                // Removing the consecutive alerts from the main alerts list
                this.basicAlerts.remove(sortedFilteredBasicAlerts.get(i));
                this.basicAlerts.remove(sortedFilteredBasicAlerts.get(i + 1));

                // Creating a new compound alert and adding it to the list of spotted alerts
                BasicAlert basicAlert = new BasicAlert(patient.getId() + "", compoundAlertName, currentTimestamp);
                alertsSpotted.add(basicAlert);
            }
        }
        return alertsSpotted;
    }


    /**
     * Removes all repeating same category alerts , informs staff about only the oldest unresolved alert
     * @param basicAlerts the list of alerts for one user
     */
    private void triggerAlerts(List<BasicAlert> basicAlerts) {
        Map<String, BasicAlert> alertMap = new HashMap<>();

        // Iterate through the alerts
        for (BasicAlert basicAlert : basicAlerts) {
            if(!(alertMap.containsKey(basicAlert.getCondition()))){
                alertMap.put(basicAlert.getCondition(), basicAlert);
            }
         }
        // Notify staff about alert
        for(BasicAlert basicAlert : alertMap.values()){
           basicAlert.triggerAlert();
        }

    }

    /**
     * Testing method
     * return all non-repeating alerts
     */
    public List<BasicAlert> getAlerts_Junit(){
        Map<String, BasicAlert> alertMap = new HashMap<>();
        // Iterate through the alerts
        for (BasicAlert basicAlert : basicAlerts) {
            if(!(alertMap.containsKey(basicAlert.getCondition()))){
                alertMap.put(basicAlert.getCondition(), basicAlert);
            }
        }

        LinkedList<BasicAlert> alertsFinal = new LinkedList(alertMap.values());
        return alertsFinal;
    }



    public static void main(String[] args) throws IOException {

       DataStorage storage = new DataStorage();
       // FilesReader s = new FilesReader("C:\\Users\\kordi\\IdeaProjects\\signal_project\\src\\test\\java\\data_management\\testFiles");
       // s.readData(storage);
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        storage.addPatientData("1",  0.18144082417659804, "ECG", 1716579056015L);
        storage.addPatientData("1",  0.7485106543877843, "ECG", 1716579057023L);
        storage.addPatientData("1",  0.2485646543877843, "ECG", 1716579058025L);
        storage.addPatientData("1", -0.1592047543877843, "ECG", 1716579059027L);
        storage.addPatientData("1", 0.3485646543877843, "ECG", 1716579061027L);
        storage.addPatientData("1", 0.1585646543877843, "ECG", 1716579062028L);


        alertGenerator.evaluateData(storage.getPatient("1"));
    }
}
