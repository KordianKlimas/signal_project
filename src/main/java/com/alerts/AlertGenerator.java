package com.alerts;

import com.data_management.DataStorage;
import com.data_management.FilesReader;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import javax.sound.sampled.Line;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    public List<Alert> alerts = new LinkedList<>();

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

        // making sure there are patients
        if (patient == null) {
            System.err.println("Null patient provided");
            return;
        }

        LinkedList<PatientRecord> data_DiastolicPressure = new LinkedList<>();
        LinkedList<PatientRecord> data_SystolicPressure = new LinkedList<>();
        LinkedList<PatientRecord> data_Saturation = new LinkedList<>();
        LinkedList<PatientRecord> data_ECG = new LinkedList<>();
        LinkedList<PatientRecord> data_WhiteBloodCells = new LinkedList<>();
        LinkedList<PatientRecord> data_Cholesterol = new LinkedList<>();
        LinkedList<PatientRecord> data_RedBloodCells = new LinkedList<>();
        this.alerts=new LinkedList<>();

        for(PatientRecord record:patient.getAllRecords()){
            switch (record.getRecordType()) {
                case "DiastolicPressure":
                    data_DiastolicPressure.add(record);
                    break;
                case "SystolicPressure":
                    data_SystolicPressure.add(record);
                    break;
                case "Saturation":
                    data_Saturation.add(record);
                    break;
                case "ECG":
                    data_ECG.add(record);
                    break;
                case "WhiteBloodCells":
                    data_WhiteBloodCells.add(record);
                    break;
                case "RedBloodCells":
                    data_RedBloodCells.add(record);
                    break;
                case "Cholesterol":
                    data_Cholesterol.add(record);
                    break;
                case "Alert":
                    String s = record.getPatientId()+"";
                    if(record.getMeasurementValue()==1){
                        alerts.add(new Alert(s,"Triggered Alert",record.getTimestamp()));
                    }
                    break;
                default:
                    System.err.println("Record type not found");
                    break;
            }
        }
        // trigger alert if certain condition are met

        this.alerts.addAll(checkBloodPressure(patient, data_DiastolicPressure, data_SystolicPressure));
        this.alerts.addAll(checkBloodSaturation(patient,data_Saturation));
        this.alerts.addAll(checkECG(patient,data_ECG));
        // compound alert:
        this.alerts.addAll(checkCompoundAlerts(patient,"SystolicPressure critical value reached","Low Saturation of oxygen in blood",0.1,"Hypotensive Hypoxemia Alert"));
        triggerAlerts(alerts);
    }

    /**
     * Checks for dangerous trends and critical patient state. Creates alert if something is could be wrong with patient.
     * When the trend is noticed only one alert will be generated for the time of when the trend started.
     * If patient's blood pressure stabilizes and the trends occurs again, new alert will be created.
     * All critical alerts will be logged and sent to staff.
     * @param patient
     * @param data_DiastolicPressure
     * @param data_SystolicPressure
     */
    private List<Alert> checkBloodPressure(Patient patient,LinkedList<PatientRecord> data_DiastolicPressure,LinkedList<PatientRecord> data_SystolicPressure ){
        //trend alert for DiastolicPressure
        final double TrendThreshold = 10; //  10mmHG increase/decrease
        final int TrendReadings= 3; // Trigger an alert if the patient's blood pressure (systolic or diastolic) shows a consistent increase or decrease across three consecutive readings
         List<Alert> AlertsSpotted = new ArrayList<>();
        //Trigger an alert if the systolic blood pressure exceeds 180
        //mmHg or drops below 90 mmHg, or if diastolic blood pressure exceeds 120 mmHg or
        //drops below 60 mmHg.
        final double CriticalThresholdd_SystolicPressure_min = 90;
        final double CriticalThresholdd_SystolicPressure_max = 180;

        final double CriticalThreshold_DiastolicPressure_min = 60;
        final double CriticalThreshold_DiastolicPressure_max = 120;

        // triggers the alert if trend or critical condition spotted
        if(!data_SystolicPressure.isEmpty()){
            int TrendCounter=0;
            double changeTemp=0;

            for(int i = 0; i<data_SystolicPressure.size()-1;i++){
                double pressureVal_current =  data_SystolicPressure.get(i).getMeasurementValue();
                double pressureVal_next =  data_SystolicPressure.get(i+1).getMeasurementValue();
                double change = pressureVal_current - pressureVal_next;

                boolean trend_alternated = changeTemp*change<0;
                changeTemp = change;

                if(Math.abs(change)>TrendThreshold && !trend_alternated){
                    TrendCounter++;
                }else{
                    TrendCounter =0;
                }
                if(TrendCounter==TrendReadings-1){

                    Alert alert = new Alert( patient.getId()+"","SystolicPressure dangerous trend",data_SystolicPressure.get(i-TrendReadings+2).getTimestamp());
                    AlertsSpotted.add(alert);
                }
                if(CriticalThresholdd_SystolicPressure_max < pressureVal_current || CriticalThresholdd_SystolicPressure_min>pressureVal_current ){
                    Alert alert = new Alert( patient.getId()+"","SystolicPressure critical value reached",data_SystolicPressure.get(i).getTimestamp());
                    AlertsSpotted.add(alert);
                }
                if(i<data_SystolicPressure.size()-1 && (CriticalThresholdd_SystolicPressure_max < pressureVal_next || CriticalThresholdd_SystolicPressure_min>pressureVal_next)){
                    Alert alert = new Alert( patient.getId()+"","SystolicPressure critical value reached",data_SystolicPressure.get(i+1).getTimestamp());
                    AlertsSpotted.add(alert);
                }
            }
            if(data_SystolicPressure.size()==1){
                double pressureVal_current = data_SystolicPressure.getFirst().getMeasurementValue();
                if(CriticalThresholdd_SystolicPressure_max < pressureVal_current || CriticalThresholdd_SystolicPressure_min>pressureVal_current ){
                    Alert alert = new Alert( patient.getId()+"","SystolicPressure critical value reached",data_SystolicPressure.getFirst().getTimestamp());
                    AlertsSpotted.add(alert);
                }
            }
        }
        if(!data_DiastolicPressure.isEmpty()){
            int TrendCounter=0;
            double changeTemp=0;

            for(int i = 0; i<data_DiastolicPressure.size()-1;i++){
                double pressureVal_current =  data_DiastolicPressure.get(i).getMeasurementValue();
                double pressureVal_next =  data_DiastolicPressure.get(i+1).getMeasurementValue();
                double change = pressureVal_current - pressureVal_next;

                boolean trend_alternated = changeTemp*change<0;
                changeTemp = change;

                if(Math.abs(change)>TrendThreshold && !trend_alternated){
                    TrendCounter++;
                }else{
                    TrendCounter =0;
                }
                if(TrendCounter==TrendReadings-1){

                    Alert alert = new Alert( patient.getId()+"","DiastolicPressure dangerous trend",data_DiastolicPressure.get(i-TrendReadings+2).getTimestamp());
                    AlertsSpotted.add(alert);
                }
                if(CriticalThreshold_DiastolicPressure_max < pressureVal_current || CriticalThreshold_DiastolicPressure_min>pressureVal_current ){
                    Alert alert = new Alert( patient.getId()+"","DiastolicPressure critical value reached",data_DiastolicPressure.get(i).getTimestamp());
                    AlertsSpotted.add(alert);
                }
                if(i<data_DiastolicPressure.size()-1 && (CriticalThreshold_DiastolicPressure_max < pressureVal_next || CriticalThreshold_DiastolicPressure_min>pressureVal_next)){
                    Alert alert = new Alert( patient.getId()+"","DiastolicPressure critical value reached",data_DiastolicPressure.get(i+1).getTimestamp());
                    AlertsSpotted.add(alert);
                }
            }
            if(data_DiastolicPressure.size()==1){
                double pressureVal_current = data_DiastolicPressure.getFirst().getMeasurementValue();
                if(CriticalThreshold_DiastolicPressure_max < pressureVal_current || CriticalThreshold_DiastolicPressure_min>pressureVal_current ){
                    Alert alert = new Alert( patient.getId()+"","DiastolicPressure critical value reached",data_DiastolicPressure.getFirst().getTimestamp());
                    AlertsSpotted.add(alert);
                }
            }
        }
        return AlertsSpotted;
    }

    /**
     * if no data within 10-minute window is present, the alert for Rapid Drop alert will not work.
     * The generated  alert  for drop has timeStamp of the first analyzed record where trend drop was found
     * ( the end of 10-minute window).
     * Creates only one alert for one 10-minute window.
     * @param patient
     * @param data_Saturation
     * @return
     */
    private List<Alert> checkBloodSaturation(Patient patient,LinkedList<PatientRecord> data_Saturation){
        List<Alert> AlertsSpotted = new ArrayList<>();
        final double CriticalThreshold_Saturation = 92;
        final double DropThreshold_Saturation = 5;
        final double window_time_minutes = 10; // 10 minutes

        int window_start_index = 0;
        boolean DropAlertCreated = false;
        for(int i=0;i<data_Saturation.size();i++){
            PatientRecord patient_record = data_Saturation.get(i);
            PatientRecord window_record=data_Saturation.get(window_start_index);

            if(patient_record.getMeasurementValue()<CriticalThreshold_Saturation){
                Alert alert = new Alert( patient.getId()+"","Low Saturation of oxygen in blood",patient_record.getTimestamp());
                AlertsSpotted.add(alert);
            }
            if(window_record.getTimestamp() - patient_record.getTimestamp()>= window_time_minutes*60 * 1000){
                for(int d = window_start_index;d<i;d++){
                    if(data_Saturation.get(d).getTimestamp() - patient_record.getTimestamp()>= window_time_minutes*60 * 1000) {
                        window_start_index =d;
                        break;
                    }
                    if(d == i-1){ //no data within 10 minute window
                        window_start_index =i;
                    }
                }

            }
            if(window_record.getMeasurementValue()>patient_record.getMeasurementValue() &&
               DropThreshold_Saturation<window_record.getMeasurementValue()-patient_record.getMeasurementValue() &&
               !DropAlertCreated
            ){
                Alert alert = new Alert( patient.getId()+"","Rapid drop of oxygen in blood",patient_record.getTimestamp());
                AlertsSpotted.add(alert);
                DropAlertCreated = true;
            }else{
                DropAlertCreated= false;
            }

        }
        return AlertsSpotted;
    }

    /**
     * Creates alert for Abnormal Heart Rate when Heart Rate drops under 50 or is over 100.
     *
     * Creates alert for Irregular beat pattern detected if in the measured data there is record two or more standard deviations.
     * To create  alert for Irregular beat pattern, the standard deviation is calculated for time window of 10 minutes, there has 
     * to be at least 5 records within 10 minutes to trigger alert .
     *
     * @param patient
     * @param ecgData
     * @return
     */
    private List<Alert> checkECG(Patient patient,LinkedList<PatientRecord> ecgData){
        final int windowSize = 10;
        final int windowTimeMinutes = 10; // in minutes. within this time the standard deviation is calculated
        final double heartRateLowerBound = 50;
        final double heartRateUpperBound = 100;
        final double varianceMultiplier = 3.6; // depends on patient . smaller value will increase number of false alerts

        List<Alert> alerts = new ArrayList<>();

        // Abnormal Heart Rate Detection
        int windowStart = 0;
        for (int i = 0; i < ecgData.size(); i++) {
            // Check if window is filled
            if (i >= windowSize - 1) {
                // Calculate BPM within the window
                double bpm = calculateBPM(ecgData.subList(windowStart, i + 1));

                // Check for abnormal heart rate
                if (bpm < heartRateLowerBound || bpm > heartRateUpperBound) {
                    PatientRecord record = ecgData.get(i);
                    Alert alert = new Alert(patient.getId() + "", "Abnormal Heart Rate "+bpm, record.getTimestamp());
                    alerts.add(alert);
                }
                // Move the window by one data point
                windowStart++;
            }
        }

        // Irregular Beat Pattern Detection
        double[] intervals = new double[ecgData.size() - 1];
        for (int i = 0; i < ecgData.size() - 1; i++) {
            intervals[i] = ecgData.get(i + 1).getTimestamp() - ecgData.get(i).getTimestamp();
        }

        double mean = Arrays.stream(intervals).average().orElse(0.0);
        double variance = Arrays.stream(intervals).map(i -> Math.pow(i - mean, 2)).average().orElse(0.0);
        double standardDeviation = Math.sqrt(variance);

        for (int i = 0; i < intervals.length; i++) {
            if (Math.abs(intervals[i] - mean) > varianceMultiplier * standardDeviation) {
                PatientRecord record = ecgData.get(i + 1);
                Alert alert = new Alert(patient.getId() + "", "Irregular Beat Pattern", record.getTimestamp());
                alerts.add(alert);
                //System.out.println("mean: "+ mean+" variance:"+ variance+" standardDeviation: "+standardDeviation + "Math.abs(intervals[i] - mean) > varianceMultiplier * standardDeviation: "+(Math.abs(intervals[i] - mean) > varianceMultiplier * standardDeviation));
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
        //System.out.println(ecgData.size() / durationMinutes);
        return (ecgData.size() / durationMinutes); // returns bpm
    }
    /**
     *  Creates compound alert  based on timeInterval, and given alerts
     * @param patient patient object
     * @param alert1
     * @param alert2
     * @param timeInterval
     * @param compoundAlertName
     * @return List<Alert>
     */
    private List<Alert> checkCompoundAlerts(Patient patient,String alert1,String alert2,double timeInterval,String compoundAlertName){
        // List to store the alerts that meet certain conditions
        List<Alert> AlertsSpotted = new ArrayList<>();

        // Filtering alerts based on specific conditions
        List<Alert> filteredList = alerts.stream()
                .filter(alert -> alert.getCondition().equals(alert1) || alert.getCondition().equals(alert2))
                .collect(Collectors.toList());

        // Sorting the filtered alerts by timestamp
        List<Alert> sortedFilteredAlerts = filteredList.stream()
                .sorted(Comparator.comparing(Alert::getTimestamp))
                .collect(Collectors.toList());

        // Iterating through the sorted filtered alerts to find consecutive alerts within a time interval
        for (int i = 0; i < sortedFilteredAlerts.size() - 1; i++) {
            long currentTimestamp = sortedFilteredAlerts.get(i).getTimestamp();
            long nextTimestamp = sortedFilteredAlerts.get(i + 1).getTimestamp();

            // Checking if the time difference between consecutive alerts is within the specified time interval
            if (nextTimestamp - currentTimestamp <= timeInterval * 60 * 1000) {
                // Removing the consecutive alerts from the main alerts list
                this.alerts.remove(sortedFilteredAlerts.get(i));
                this.alerts.remove(sortedFilteredAlerts.get(i + 1));

                // Creating a new compound alert and adding it to the list of spotted alerts
                Alert alert = new Alert(patient.getId() + "", compoundAlertName, currentTimestamp);
                AlertsSpotted.add(alert);
            }
        }
        return  AlertsSpotted;
    }

    /**
     * Removes all repeating same category alerts , informs staff about only the oldest unresolved alert
     * @param alerts the list of alerts for one user
     */
    private void triggerAlerts(List<Alert> alerts) {
        Map<String, Alert> alertMap = new HashMap<>();

        // Iterate through the alerts
        for (Alert alert : alerts) {
            if(!(alertMap.containsKey(alert.getCondition()))){
                alertMap.put(alert.getCondition(),alert);
            }
         }
        // Notify staff about alert
        for(Alert alert : alertMap.values()){
            System.out.println("Alert:");
            System.out.println(alert.getPatientId()+" "+ alert.getCondition()+" "+ alert.getTimestamp());
        }

    }

    /**
     * Testing method
     */
    public List<Alert> getAlerts_Junit(){
        return this.alerts;
    }



    public static void main(String[] args) throws IOException {

        DataStorage storage = new DataStorage();
        FilesReader s = new FilesReader("C:\\Users\\kordi\\IdeaProjects\\signal_project\\src\\test\\java\\data_management\\testFiles");
        s.readData(storage);
        AlertGenerator alertGenerator = new AlertGenerator(storage);


        alertGenerator.evaluateData(storage.getPatient(1));
        alertGenerator.evaluateData(storage.getPatient(2));
    }
}
