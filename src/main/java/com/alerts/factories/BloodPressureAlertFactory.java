package com.alerts.factories;

import com.alerts.decorators.BasicAlert;

public class BloodPressureAlertFactory extends AlertFactory{
    @Override

    public BasicAlert createAlert(String patientId, String condition, long timestamp) {
        return new BasicAlert(patientId, condition, timestamp);
    }

}
