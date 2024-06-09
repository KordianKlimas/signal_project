package com.alerts.decorators;

public interface Alert {
    String getPatientId();
    String getCondition();
    long getTimestamp();

    public void triggerAlert();


}

