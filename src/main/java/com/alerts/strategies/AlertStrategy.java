package com.alerts.strategies;

import com.alerts.decorators.BasicAlert;
import com.data_management.Patient;

import java.util.List;

/**
 * An interface for alert strategies.
 */
public interface AlertStrategy {
    /**
     * Checks for alerts within a specified time range for a given patient.
     *
     * @param patient   The patient for whom alerts are checked.
     * @param startTime The start time of the time range.
     * @param endTime   The end time of the time range.
     * @return A list of BasicAlert objects representing the alerts found.
     */
    List<BasicAlert> checkAlert(Patient patient, long startTime, long endTime);
}
