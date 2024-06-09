package com.alerts.decorators;

public class AlertDecorator extends BasicAlert implements Alert {
    private Alert decoratedAlert;
    public AlertDecorator(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }

    public void triggerAlert() {
        decoratedAlert.triggerAlert();
    }
}
