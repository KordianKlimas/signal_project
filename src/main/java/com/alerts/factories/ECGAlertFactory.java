package com.alerts.factories;

import com.alerts.decorators.BasicAlert;

/**
 * A factory for creating ECG alerts.
 */
public class ECGAlertFactory extends AlertFactory {

    /**
     * Creates a basic alert for ECG.
     *
     * @param patientId The patient ID associated with the alert.
     * @param condition The condition that triggered the alert.
     * @param timestamp The timestamp when the alert was triggered.
     * @return The created BasicAlert object.
     */
    @Override
    public BasicAlert createAlert(String patientId, String condition, long timestamp) {
        return new BasicAlert(patientId, condition, timestamp);
    }

}
