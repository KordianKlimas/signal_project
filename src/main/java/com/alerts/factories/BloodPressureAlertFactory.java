package com.alerts.factories;

import com.alerts.Alert;

public class BloodPressureAlertFactory extends AlertFactory{
    @Override
    Alert createAlert(String patientId, String condition, long timestamp) {
        return null;
    }
}
