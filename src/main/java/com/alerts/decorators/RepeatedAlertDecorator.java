package com.alerts.decorators;

/**
 * A decorator for repeating alerts.
 */
public class RepeatedAlertDecorator extends AlertDecorator {

    /**
     * Constructs a new RepeatedAlertDecorator object.
     */
    public RepeatedAlertDecorator(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }

    /**
     * Triggers the alert with a "Repeated" prefix.
     */
    @Override
    public void triggerAlert() {
        System.out.print("Repeated ");
        super.triggerAlert();
    }
}
