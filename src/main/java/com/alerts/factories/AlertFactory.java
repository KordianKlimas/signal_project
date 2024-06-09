package com.alerts.factories;

import com.alerts.decorators.BasicAlert;

/**
 * An abstract factory for creating alerts.
 */
abstract public class AlertFactory {

    /**
     * Creates a basic alert.
     *
     * @param patientId The patient ID associated with the alert.
     * @param condition The condition that triggered the alert.
     * @param timestamp The timestamp when the alert was triggered.
     * @return The created BasicAlert object.
     */
    abstract BasicAlert createAlert(String patientId, String condition, long timestamp);

}
