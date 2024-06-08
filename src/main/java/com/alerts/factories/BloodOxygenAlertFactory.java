package com.alerts.factories;

import com.alerts.Alert;

public class BloodOxygenAlertFactory extends AlertFactory{
    @Override
    Alert createAlert(String patientId, String condition, long timestamp) {
        return null;
    }
}
