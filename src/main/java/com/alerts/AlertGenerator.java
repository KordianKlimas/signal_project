package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import javax.sound.sampled.Line;
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
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        // the alert definition  methods  could be replaced with different more scalable approach that would allow to create new types of
        // record types beside the 7 that are currently supported ( Cholesterol, ECG.....) and
        // check compound alerts created by staff


        LinkedList<PatientRecord> data_DiastolicPressure = new LinkedList<>();
        LinkedList<PatientRecord> data_SystolicPressure = new LinkedList<>();
        LinkedList<PatientRecord> data_Saturation = new LinkedList<>();
        LinkedList<PatientRecord> data_ECG = new LinkedList<>();
        LinkedList<PatientRecord> data_WhiteBloodCells = new LinkedList<>();
        LinkedList<PatientRecord> data_Cholesterol = new LinkedList<>();
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
        //this.alerts.addAll(checkECG(patient,data_ECG));
        // compound alert:
        this.alerts.addAll(checkCompoundAlerts(patient,"SystolicPressure critical value reached","Low Saturation of oxygen in blood",0.1,"Hypotensive Hypoxemia Alert"));

        for(PatientRecord record:data_ECG){
        //  System.out.println(convertToBPM(record.getMeasurementValue()));
        }

        for(Alert alert: alerts){
            triggerAlert(alert);
        }
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
     * Creates alert for Irregular beat pattern detected if in the measured data there is record two or more standard deviations.
     * To create  alert for Irregular beat pattern, the standard deviation is calculated for time window of 10 minutes, there has 
     * to be at leat 5 records within 10 minutes to trigger alert.
     *
     * @param patient
     * @param data_ECG
     * @return
     */
    private List<Alert> checkECG(Patient patient,LinkedList<PatientRecord> data_ECG){
        final int windowSize = 10;
        final int window_time_minutes = 10; // in minutes. withing this time the  standard deviation is calculated 

        List<Alert> AlertsSpotted = new ArrayList<>();
        for (int i = 1; i < data_ECG.size(); i++) {
            double heartRate = data_ECG.get(i).getMeasurementValue();
            if(heartRate < 50 || heartRate > 100) {
                Alert alert = new Alert( patient.getId()+"","Abnormal Heart Rate detected",data_ECG.get(i).getTimestamp());
                AlertsSpotted.add(alert);
            }
        }
        int takenIntoAccount;
        for (int i = 0; i <= data_ECG.size() - windowSize; i++) {
            takenIntoAccount=0;
            double sum = 0;
            double variance = 0;
            for (int j = i; j < i + windowSize; j++) {
                if (data_ECG.get(i).getTimestamp() - data_ECG.get(j).getTimestamp() >= window_time_minutes * 60 * 1000) {
                    sum += data_ECG.get(j).getMeasurementValue();

                    takenIntoAccount++;
                }
            }
            //too little data in time interval
            if(takenIntoAccount<5){
                break;
            }
            double avarage = sum/takenIntoAccount;
            for(int k = i; k < i + windowSize; k++) {
                if (data_ECG.get(i).getTimestamp() - data_ECG.get(k).getTimestamp() >= window_time_minutes * 60 * 1000) {
                    variance += Math.pow(data_ECG.get(k).getMeasurementValue() - avarage, 2);
                }
            }
            variance = variance/takenIntoAccount;
            double treshold =  avarage -2*Math.sqrt(variance);
            // if measurement is 2 variances away the patient could be in dangerous state
            for(int d = i; d < i + windowSize; d++) {
                if (data_ECG.get(i).getTimestamp() - data_ECG.get(d).getTimestamp() >= window_time_minutes * 60 * 1000) {
                    if(treshold<Math.abs(data_ECG.get(d).getMeasurementValue())){
                        Alert alert = new Alert( patient.getId()+"","Irregular beat pattern detected",data_ECG.get(d).getTimestamp());
                        AlertsSpotted.add(alert);
                    }
                }
            }

        }

            //movingAverages[i] = sum / windowSize;


        return AlertsSpotted;
    }

    /**
     * Replaces alerts and by new compound alert
     * @param patient patient object
     * @param alert1
     * @param alert2
     * @param timeInterval
     * @param compoundAlertName
     * @return
     */
    private List<Alert> checkCompoundAlerts(Patient patient,String alert1,String alert2,double timeInterval,String compoundAlertName){
        List<Alert> AlertsSpotted = new ArrayList<>();
        List<Alert> filteredList = alerts.stream()
                .filter(alert -> alert.getCondition().equals(alert1) || alert.getCondition().equals(alert2))
                .collect(Collectors.toList());
        List<Alert> sortedFilteredAlerts = filteredList.stream()
                .sorted(Comparator.comparing(Alert::getTimestamp))
                .collect(Collectors.toList());

        for (int i = 0; i < sortedFilteredAlerts.size() - 1; i++) {
            long currentTimestamp = sortedFilteredAlerts.get(i).getTimestamp();
            long nextTimestamp = sortedFilteredAlerts.get(i + 1).getTimestamp();
            if (nextTimestamp - currentTimestamp <=timeInterval* 60 * 1000) {
                this.alerts.remove(sortedFilteredAlerts.get(i));
                this.alerts.remove(sortedFilteredAlerts.get(i+1));
                Alert alert = new Alert( patient.getId()+"",compoundAlertName,currentTimestamp);
                AlertsSpotted.add(alert);
            }
        }
        return  AlertsSpotted;
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
     System.out.println("Alert:");
     System.out.println(alert.getPatientId()+" "+ alert.getCondition()+" "+ alert.getTimestamp());
    }

    public List<Alert> getAlerts_Junit(){
        return this.alerts;
    }



    public static void main(String[] args){
        DataStorage storage = new DataStorage();
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // how to take care of ECG values???
        // how to transform them into BMP???
        storage.addPatientData(1, 0.6099302683076968, "ECG", 1715178774510L);
        storage.addPatientData(1, 0.18189252946664133, "ECG", 1715178775512L);
        storage.addPatientData(1, 0.28493005767359203, "ECG", 1715178776522L);
        storage.addPatientData(1, 0.2705057971260119, "ECG", 1715178777521L);
        storage.addPatientData(1, -0.6143246583976606, "ECG", 1715178778517L);
        storage.addPatientData(1, -0.5040335019366801, "ECG", 1715178779520L);
        storage.addPatientData(1, 0.45778429862016323, "ECG", 1715178780519L);
        storage.addPatientData(1, -0.0032713216698692996, "ECG",1715178781517L);
        storage.addPatientData(1, 0.21932113839759076, "ECG", 1715178782521L);
        storage.addPatientData(1, -0.5126986621406572, "ECG", 1715178783514L);
        storage.addPatientData(1, 0.3023332642800313, "ECG", 1715178784515L);
        storage.addPatientData(1, -0.5021229515606025, "ECG", 1715178785520L);
        storage.addPatientData(1, 0.2662856718090292, "ECG", 1715178786521L);
        storage.addPatientData(1, -0.05253255057103075, "ECG", 1715178787517L);


        alertGenerator.evaluateData(storage.getPatient(1));
        System.out.println(alertGenerator.getAlerts_Junit());
    }
}
