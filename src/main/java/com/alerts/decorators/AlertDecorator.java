package com.alerts.decorators;

/**
 * A decorator for extending basic alert functionality.
 */
public class AlertDecorator extends BasicAlert {

    private Alert decoratedAlert;

    /**
     * Constructs a new AlertDecorator object.
     */
    public AlertDecorator(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }

    /**
     * Triggers the alert.
     * This method delegates the triggering of the alert to the decorated alert object.
     */
    @Override
    public void triggerAlert() {
        decoratedAlert.triggerAlert();
    }
}
