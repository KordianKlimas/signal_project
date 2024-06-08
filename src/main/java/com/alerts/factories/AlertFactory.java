package com.alerts.factories;

import com.alerts.Alert;

abstract public class AlertFactory {

    abstract Alert createAlert(String patientId, String condition, long timestamp);

}

