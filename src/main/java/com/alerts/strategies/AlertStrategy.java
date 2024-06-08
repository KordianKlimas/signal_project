package com.alerts.strategies;

import com.alerts.Alert;

public interface AlertStrategy {
    public Alert checkAlert(String patientId, String condition, long timestamp);
}
