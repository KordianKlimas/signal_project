package com.alerts.strategies;

import com.alerts.decorators.BasicAlert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;

public class BloodPressureStrategy extends AlertGenerator implements AlertStrategy {
    private DataStorage dataStorage;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public BloodPressureStrategy(DataStorage dataStorage) {
        super(dataStorage);
        this.dataStorage = dataStorage;
    }

    @Override
    public List<BasicAlert> checkAlert(Patient patient, long startTime, long endTime) {
        return checkBloodPressure(patient,startTime,endTime);
    }

    private List<BasicAlert> checkBloodPressure(Patient patient, long startTime, long endTime ){
        // preparing  data
        String patientId = patient.getId();
        List<PatientRecord> systolicRecords = dataStorage.getRecords(patientId, startTime, endTime)
                .parallelStream()
                .filter(record -> "SystolicPressure".equals(record.getRecordType()))
                .sorted(comparingLong(PatientRecord::getTimestamp).reversed())
                .collect(toList());
        List<PatientRecord> DiastolicRecords = dataStorage.getRecords(patientId, startTime, endTime)
                .parallelStream()
                .filter(record -> "DiastolicPressure".equals(record.getRecordType()))
                .sorted(comparingLong(PatientRecord::getTimestamp).reversed())
                .collect(toList());


        //trend alert for DiastolicPressure
        final double TrendThreshold = 10; //  10mmHG increase/decrease
        final int TrendReadings= 3; // Trigger an alert if the patient's blood pressure (systolic or diastolic) shows a consistent increase or decrease across three consecutive readings
        List<BasicAlert> alertsSpotted = new ArrayList<>();
        //Trigger an alert if the systolic blood pressure exceeds 180
        //mmHg or drops below 90 mmHg, or if diastolic blood pressure exceeds 120 mmHg or
        //drops below 60 mmHg.
        final double CriticalThreshold_SystolicPressure_min = 90;
        final double CriticalThreshold_SystolicPressure_max = 180;

        final double CriticalThreshold_DiastolicPressure_min = 60;
        final double CriticalThreshold_DiastolicPressure_max = 120;

        // triggers the alert if trend or critical condition spotted
        if(!systolicRecords.isEmpty()){
            int TrendCounter=0;
            double changeTemp=0;

            for(int i = 0; i<systolicRecords.size()-1;i++){
                double pressureVal_current =  systolicRecords.get(i).getMeasurementValue();
                double pressureVal_next =  systolicRecords.get(i+1).getMeasurementValue();
                double change = pressureVal_current - pressureVal_next;

                boolean trend_alternated = changeTemp*change<0;
                changeTemp = change;

                if(Math.abs(change)>TrendThreshold && !trend_alternated){
                    TrendCounter++;
                }else{
                    TrendCounter =0;
                }
                if(TrendCounter==TrendReadings-1){

                    BasicAlert basicAlert = new BasicAlert( patientId,"SystolicPressure dangerous trend",systolicRecords.get(i-TrendReadings+2).getTimestamp());
                    alertsSpotted.add(basicAlert);
                }
                if(CriticalThreshold_SystolicPressure_max < pressureVal_current || CriticalThreshold_SystolicPressure_min>pressureVal_current ){
                    BasicAlert basicAlert = new BasicAlert( patientId,"SystolicPressure critical value reached",systolicRecords.get(i).getTimestamp());
                    alertsSpotted.add(basicAlert);
                }
                if(i<systolicRecords.size()-1 && (CriticalThreshold_SystolicPressure_max < pressureVal_next || CriticalThreshold_SystolicPressure_min>pressureVal_next)){
                    BasicAlert basicAlert = new BasicAlert( patientId,"SystolicPressure critical value reached",systolicRecords.get(i+1).getTimestamp());
                    alertsSpotted.add(basicAlert);
                }
            }
            if(systolicRecords.size()==1){
                double pressureVal_current = systolicRecords.get(0).getMeasurementValue();
                if(CriticalThreshold_SystolicPressure_max < pressureVal_current || CriticalThreshold_SystolicPressure_min>pressureVal_current ){
                    BasicAlert basicAlert = new BasicAlert( patientId,"SystolicPressure critical value reached",systolicRecords.get(0).getTimestamp());
                    alertsSpotted.add(basicAlert);
                }
            }
        }
        if(!DiastolicRecords.isEmpty()){
            int TrendCounter=0;
            double changeTemp=0;

            for(int i = 0; i<DiastolicRecords.size()-1;i++){
                double pressureVal_current =  DiastolicRecords.get(i).getMeasurementValue();
                double pressureVal_next =  DiastolicRecords.get(i+1).getMeasurementValue();
                double change = pressureVal_current - pressureVal_next;

                boolean trend_alternated = changeTemp*change<0;
                changeTemp = change;

                if(Math.abs(change)>TrendThreshold && !trend_alternated){
                    TrendCounter++;
                }else{
                    TrendCounter =0;
                }
                if(TrendCounter==TrendReadings-1){

                    BasicAlert basicAlert = new BasicAlert( patientId,"DiastolicPressure dangerous trend",DiastolicRecords.get(i-TrendReadings+2).getTimestamp());
                    alertsSpotted.add(basicAlert);
                }
                if(CriticalThreshold_DiastolicPressure_max < pressureVal_current || CriticalThreshold_DiastolicPressure_min>pressureVal_current ){
                    BasicAlert basicAlert = new BasicAlert( patientId,"DiastolicPressure critical value reached",DiastolicRecords.get(i).getTimestamp());
                    alertsSpotted.add(basicAlert);
                }
                if(i<DiastolicRecords.size()-1 && (CriticalThreshold_DiastolicPressure_max < pressureVal_next || CriticalThreshold_DiastolicPressure_min>pressureVal_next)){
                    BasicAlert basicAlert = new BasicAlert( patientId,"DiastolicPressure critical value reached",DiastolicRecords.get(i+1).getTimestamp());
                    alertsSpotted.add(basicAlert);
                }
            }
            if(DiastolicRecords.size()==1){
                double pressureVal_current = DiastolicRecords.get(0).getMeasurementValue();
                if(CriticalThreshold_DiastolicPressure_max < pressureVal_current || CriticalThreshold_DiastolicPressure_min>pressureVal_current ){
                    BasicAlert basicAlert = new BasicAlert( patientId,"DiastolicPressure critical value reached",DiastolicRecords.get(0).getTimestamp());
                    alertsSpotted.add(basicAlert);
                }
            }
        }
        return alertsSpotted;
    }

}
