package data_management;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EvaluateDataTest {
    // Helper method for Alert check
    private boolean alertListsAreEqual(List<Alert> list_1, List<Alert> list_2) {
        if (list_1.size() != list_2.size()) {
            return false;
        }

        for (int i = 0; i < list_1.size(); i++) {
            // Check for equality of each field of the Alert objects
            if (     list_1.get(i).getTimestamp()!=(list_2.get(i).getTimestamp() ) ||
                    !list_1.get(i).getCondition().equals(list_2.get(i).getCondition()) ||
                    !list_1.get(i).getPatientId().equals(list_2.get(i).getPatientId())) {
                return false; // Return false if any field is not equal
            }
        }

        return true; // Return true if all elements are equal
      }
    /**
     *  (SystolicPressure and DiastolicPressure)
     *  Patient with good blood results
     */
    @Test
    void  bloodPressureAlertsTest_1() {
        DataStorage storage = new DataStorage();
        AlertGenerator alertGenerator = new AlertGenerator(storage);


        storage.addPatientData(1, 90, "DiastolicPressure",1714376789050L);
        storage.addPatientData(1, 101,"DiastolicPressure", 1714376789051L);
        storage.addPatientData(1, 90, "DiastolicPressure",1714376789052L);
        storage.addPatientData(1, 102,"DiastolicPressure", 1714376789053L);

        storage.addPatientData(1, 180,"SystolicPressure",   1714376789050L);
        storage.addPatientData(1, 168, "SystolicPressure",  1714376789051L);
        storage.addPatientData(1, 180,"SystolicPressure",   1714376789052L);
        storage.addPatientData(1, 160, "SystolicPressure",  1714376789053L);

        alertGenerator.evaluateData(storage.getPatient(1));


        List<Alert> alerts = alertGenerator.getAlerts_Junit();
        System.out.println(alertGenerator.getAlerts_Junit());
        assertTrue(alerts.isEmpty(), "No alerts expected for a healthy patient");
    }
    /**
     * (SystolicPressure or DiastolicPressure)
     * testing for critical state.
     */
    @Test
    void  bloodPressureAlertsTest_2() {
        DataStorage storage = new DataStorage();
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // getting alerts from each patient's data evaluation
        storage.addPatientData(1, 59, "DiastolicPressure",1714376789050L);
        storage.addPatientData(2, 121,"DiastolicPressure", 1714376789051L);

        storage.addPatientData(3, 89,"SystolicPressure",   1714376789050L);
        storage.addPatientData(4, 181, "SystolicPressure",  1714376789051L);

        alertGenerator.evaluateData(storage.getPatient(1));
        List<Alert> alerts = alertGenerator.getAlerts_Junit();

        alertGenerator.evaluateData(storage.getPatient(2));
        alerts.addAll(alertGenerator.getAlerts_Junit());

        alertGenerator.evaluateData(storage.getPatient(3));
        alerts.addAll(alertGenerator.getAlerts_Junit());

        alertGenerator.evaluateData(storage.getPatient(4));
        alerts.addAll(alertGenerator.getAlerts_Junit());


        // Expected alerts for patients
        List<Alert> alertsTests = new ArrayList<>();
        Alert alert1_test = new Alert("1","DiastolicPressure critical value reached",1714376789050L);
        Alert alert2_test = new Alert("2","DiastolicPressure critical value reached", 1714376789051L);
        Alert alert3_test = new Alert("3","SystolicPressure critical value reached", 1714376789050L);
        Alert alert4_test = new Alert("4","SystolicPressure critical value reached",  1714376789051L);
        alertsTests.add(alert1_test);
        alertsTests.add(alert2_test);
        alertsTests.add(alert3_test);
        alertsTests.add(alert4_test);

        // test if Expected alerts match with generated ones
        assertTrue(alertListsAreEqual(alerts,alertsTests), "4 different alerts expected for patients");
    }

    /**
     * (SystolicPressure or DiastolicPressure)
     * Dangerous trends
     */
    @Test
    void  bloodPressureAlertsTest_3() {
        DataStorage storage = new DataStorage();
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // getting alerts from each patient's data evaluation

        //decreasing trend
        storage.addPatientData(1, 90, "SystolicPressure",1714376789050L);
        storage.addPatientData(1, 101, "SystolicPressure",1714376789051L);
        storage.addPatientData(1, 112, "SystolicPressure",1714376789052L);
        storage.addPatientData(1, 123, "SystolicPressure",1714376789053L);


        storage.addPatientData(2, 60, "DiastolicPressure",1714376789050L);
        storage.addPatientData(2, 71, "DiastolicPressure",1714376789051L);
        storage.addPatientData(2, 82, "DiastolicPressure",1714376789052L);
        storage.addPatientData(2, 93, "DiastolicPressure",1714376789053L);

        //increasing trend
        storage.addPatientData(3, 123, "SystolicPressure",1714376789050L);
        storage.addPatientData(3, 112, "SystolicPressure",1714376789051L);
        storage.addPatientData(3, 101, "SystolicPressure",1714376789052L);
        storage.addPatientData(3, 90, "SystolicPressure",1714376789053L);


        storage.addPatientData(4, 93, "DiastolicPressure",1714376789050L);
        storage.addPatientData(4, 82, "DiastolicPressure",1714376789051L);
        storage.addPatientData(4, 71, "DiastolicPressure",1714376789052L);
        storage.addPatientData(4, 60, "DiastolicPressure",1714376789053L);

        alertGenerator.evaluateData(storage.getPatient(1));
        List<Alert> alerts = alertGenerator.getAlerts_Junit();

        alertGenerator.evaluateData(storage.getPatient(2));
        alerts.addAll(alertGenerator.getAlerts_Junit());

        alertGenerator.evaluateData(storage.getPatient(3));
        alerts.addAll(alertGenerator.getAlerts_Junit());

        alertGenerator.evaluateData(storage.getPatient(4));
        alerts.addAll(alertGenerator.getAlerts_Junit());


        // Expected alerts for patients
        List<Alert> alertsTests = new ArrayList<>();
        Alert alert1_test = new Alert("1","SystolicPressure dangerous trend",1714376789050L);
        Alert alert2_test = new Alert("2","DiastolicPressure dangerous trend", 1714376789050L);
        Alert alert3_test = new Alert("3","SystolicPressure dangerous trend", 1714376789050L);
        Alert alert4_test = new Alert("4","DiastolicPressure dangerous trend",  1714376789050L);
        alertsTests.add(alert1_test);
        alertsTests.add(alert2_test);
        alertsTests.add(alert3_test);
        alertsTests.add(alert4_test);

        // test if Expected alerts match with generated ones
        assertTrue(alertListsAreEqual(alerts,alertsTests), "4 different alerts expected for patients");
    }


    /**
     * (Saturation of oxygen in blood)
     * Not dangerous increase of oxygen within 10 minutes
     */
    @Test
    void  SaturationAlertsTest_1() {
        DataStorage storage = new DataStorage();
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // not dangerous increase
        storage.addPatientData(1, 93, "Saturation",1714376789052L);
        storage.addPatientData(1, 95, "Saturation",1714377089052L);
        storage.addPatientData(1, 99, "Saturation",1714376249052L);

        // Generated alerts for patients
        alertGenerator.evaluateData(storage.getPatient(1));
        List<Alert> alerts = alertGenerator.getAlerts_Junit();

        // Expected alerts for patients
        List<Alert> alertsTests = new ArrayList<>();


        // test if Expected alerts match with generated ones
        assertTrue(alertListsAreEqual(alerts,alertsTests), "Wrong diagnosis of not dangerous state");
    }
    /**
     * (Saturation of oxygen in blood)
     * Dangerous Drop of oxygen within 10 minutes
     */
    @Test
    void  SaturationAlertsTest_2() {
        DataStorage storage = new DataStorage();
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // Evaluating data
        storage.addPatientData(1, 99, "Saturation",1714376789052L); // minute: 0
        storage.addPatientData(1, 95, "Saturation",1714377089052L); // minute: 4
        storage.addPatientData(1, 93, "Saturation",1714376249052L); // minute: 9
        storage.addPatientData(1, 93, "Saturation",1714376249053L); // minute: 9
        // Generated alerts for patients
        alertGenerator.evaluateData(storage.getPatient(1));
        List<Alert> alerts = alertGenerator.getAlerts_Junit();

        // Expected alerts for patients
        List<Alert> alertsTests = new ArrayList<>();
        Alert alert1_test = new Alert("1","Rapid drop of oxygen in blood",1714376249052L);
        alertsTests.add(alert1_test);

        // test if Expected alerts match with generated ones
        assertTrue(alertListsAreEqual(alerts,alertsTests), "Rapid drop of oxygen in blood not recognized");
    }
    /**
     * (Saturation of oxygen in blood)
     * Dangerous Drop of oxygen within  10 minutes starting at 4'th minute
     */
    @Test
    void  SaturationAlertsTest_3() {
        DataStorage storage = new DataStorage();
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // not dangerous increase
        storage.addPatientData(1, 99, "Saturation",1714376789052L); // minute: 0
        storage.addPatientData(1, 99, "Saturation",1714377089052L); // minute: 4
        storage.addPatientData(1, 99, "Saturation",1714376249052L); // minute: 9
        storage.addPatientData(1, 92, "Saturation",1714376429052L); // minute: 12

        // Generated alerts for patients
        alertGenerator.evaluateData(storage.getPatient(1));
        List<Alert> alerts = alertGenerator.getAlerts_Junit();

        // Expected alerts for patients
        List<Alert> alertsTests = new ArrayList<>();
        Alert alert1_test = new Alert("1","Rapid drop of oxygen in blood",1714376429052L);
        alertsTests.add(alert1_test);


        // test if Expected alerts match with generated ones
        assertTrue(alertListsAreEqual(alerts,alertsTests), "Wrong diagnosis of Dangerous Drop of oxygen within  10 minutes starting at 4'th minute");
    }
    /**
     * (Saturation of oxygen in blood)
     * not Dangerous fluctuating oxygen levels
     */
    @Test
    void  SaturationAlertsTest_4() {
        DataStorage storage = new DataStorage();
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // not dangerous increase
        storage.addPatientData(1, 99, "Saturation",1714376789052L); // minute: 0
        storage.addPatientData(1, 96, "Saturation",1714377089052L); // minute: 4
        storage.addPatientData(1, 97, "Saturation",1714376249052L); // minute: 9
        storage.addPatientData(1, 95, "Saturation",1714376429052L); // minute: 12

        // Generated alerts for patients
        alertGenerator.evaluateData(storage.getPatient(1));
        List<Alert> alerts = alertGenerator.getAlerts_Junit();

        // Expected alerts for patients
        List<Alert> alertsTests = new ArrayList<>();


        // test if Expected alerts match with generated ones
        assertTrue(alertListsAreEqual(alerts,alertsTests), "Wrong diagnosis of not dangerous state");
    }
    /**
     * (Saturation of oxygen in blood)
     * not Dangerous fluctuating oxygen levels
     */
    @Test
    void  HypotensiveHypoxemiaTest_1() {
        DataStorage storage = new DataStorage();
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // not dangerous increase
        storage.addPatientData(1, 89, "Saturation",1714376789052L); // same time
        storage.addPatientData(1, 89, "SystolicPressure",1714376789052L);// same time


        // Generated alerts for patients
        alertGenerator.evaluateData(storage.getPatient(1));
        List<Alert> alerts = alertGenerator.getAlerts_Junit();

        // Expected alerts for patients
        List<Alert> alertsTests = new ArrayList<>();
        Alert alert = new Alert("1","Hypotensive Hypoxemia Alert",1714376789052L);
        alertsTests.add(alert);
        // test if Expected alerts match with generated ones
        assertTrue(alertListsAreEqual(alerts,alertsTests), "Hypotensive Hypoxemia not diagnosed");
    }
    /**
     * (Saturation of oxygen in blood)
     * not Dangerous fluctuating oxygen levels
     */
    @Test
    void  StaffAlertTest_1() {
        DataStorage storage = new DataStorage();
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // staff alert
        storage.addPatientData(1, 1, "Alert",1714376789052L); // same time

        // Generated alerts for patients
        alertGenerator.evaluateData(storage.getPatient(1));
        List<Alert> alerts = alertGenerator.getAlerts_Junit();

        // Expected alerts for patients
        List<Alert> alertsTests = new ArrayList<>();
        Alert alert = new Alert("1","Triggered Alert",1714376789052L);
        alertsTests.add(alert);

        // test if Expected alerts match with generated ones
        assertTrue(alertListsAreEqual(alerts,alertsTests), "Hypotensive Hypoxemia not diagnosed");
    }
}





