package com.alerts.decorators;

import com.alerts.factories.AlertFactory;

// Represents an alert
public class BasicAlert  implements Alert {
    private String patientId;
    private String condition;
    private long timestamp;

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
    @Override
    public void triggerAlert() {
        System.out.println("Alert: "+this.patientId+" "+ this.condition+" "+ this.timestamp);
    }
}
