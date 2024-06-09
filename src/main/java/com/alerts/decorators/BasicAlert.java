package com.alerts.decorators;

/**
 * Represents a basic alert.
 */
public class BasicAlert implements Alert {

    private String patientId;
    private String condition;
    private long timestamp;

    /**
     * Constructs a new BasicAlert object.
     */
    public BasicAlert(String patientId, String condition, long timestamp) {
        this.patientId = patientId;
        this.condition = condition;
        this.timestamp = timestamp;
    }

    @Override
    public String getPatientId() {
        return patientId;
    }

    @Override
    public String getCondition() {
        return condition;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Triggers the alert.
     */
    @Override
    public void triggerAlert() {
        System.out.println("Alert: " + this.patientId + " " + this.condition + " " + this.timestamp);
    }
}
