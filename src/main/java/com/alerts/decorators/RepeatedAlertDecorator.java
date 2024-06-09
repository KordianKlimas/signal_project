package com.alerts.decorators;

public class RepeatedAlertDecorator extends AlertDecorator implements Alert{

    public RepeatedAlertDecorator(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
    @Override
    public void triggerAlert() {
        System.out.print("Repeated ");
        super.triggerAlert();
    }
}
