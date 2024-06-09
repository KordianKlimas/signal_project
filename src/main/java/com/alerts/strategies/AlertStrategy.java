package com.alerts.strategies;

import com.alerts.Alert;
import com.data_management.Patient;

import java.util.List;

public interface AlertStrategy {
    /**
     * Checks for alerts
     * @param patient
     * @param startTime
     * @param endTime
     * @return
     */
    public List<Alert> checkAlert(Patient patient, long startTime, long endTime);
}
