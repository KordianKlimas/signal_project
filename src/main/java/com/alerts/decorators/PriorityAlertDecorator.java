package com.alerts.decorators;

public class PriorityAlertDecorator extends AlertDecorator implements Alert{
    public PriorityAlertDecorator(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
    @Override
    public void triggerAlert() {
        System.out.print("Priority ");
        super.triggerAlert();
    }
}
