package com.alerts.factories;

import com.alerts.decorators.BasicAlert;

abstract public class AlertFactory {

    abstract BasicAlert createAlert(String patientId, String condition, long timestamp);

}

