package com.alerts.factories;

import com.alerts.Alert;

public class ECGAlertFactory extends AlertFactory{
    @Override
    Alert createAlert(String patientId, String condition, long timestamp) {
        return null;
    }
}
