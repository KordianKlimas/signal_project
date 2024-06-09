package com.alerts.decorators;

/**
 * Represents an alert in the system.
 */
public interface Alert {

    /**
     * Retrieves the patient ID associated with this alert.
     *
     * @return The patient ID.
     */
    String getPatientId();

    /**
     * Retrieves the condition that triggered this alert.
     *
     * @return The condition.
     */
    String getCondition();

    /**
     * Retrieves the timestamp when this alert was triggered.
     *
     * @return The timestamp.
     */
    long getTimestamp();

    /**
     * Triggers the alert.
     */
    void triggerAlert();
}
