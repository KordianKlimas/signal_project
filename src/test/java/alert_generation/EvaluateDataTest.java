package alert_generation;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.FilesReader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


class EvaluateDataTest {
    // Helper method for Alert check
    private boolean alertListsAreEqual(List<Alert> list_1, List<Alert> list_2) {
        if (list_1.size() != list_2.size()) {

            return false;
        }

        for (int i = 0; i < list_1.size(); i++) {
            if (    list_1.get(i).getTimestamp()==(list_2.get(i).getTimestamp() ) &&
                    list_1.get(i).getCondition().equals(list_2.get(i).getCondition()) &&
                    list_1.get(i).getPatientId() == list_2.get(i).getPatientId()) {
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
        assertTrue(alertListsAreEqual(alerts,alertsTests), "Triggered Alert not forwarded to staff");
    }

    /**
     *
     * ECG test
     * Healthy patient
     */
    @Test
    void  ECG_test_1() {
        DataStorage storage = new DataStorage();
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // staff alert
        storage.addPatientData(1,  -0.45947993431497647, "ECG",1716579044014L);
        storage.addPatientData(1,  0.751036609873168, "ECG", 1716579045024L);
        storage.addPatientData( 1,  0.7715715261423168, "ECG", 1716579046022L);
        storage.addPatientData(1,  0.2134302739543229, "ECG", 1716579047014L);
        storage.addPatientData(1,  -0.38711157663843443, "ECG",1716579048020L);
        storage.addPatientData(1,  0.5323740342562988, "ECG", 1716579049013L);
        storage.addPatientData(1,  0.758495186616306, "ECG", 1716579050022L);
        storage.addPatientData(1,  0.4548244972706137, "ECG",1716579051021L);
        storage.addPatientData(1,  -0.4802269932870855, "ECG", 1716579052015L);
        storage.addPatientData(1,  -0.1802198165576428, "ECG", 1716579053024L);
        storage.addPatientData(1,  0.27809302908830535, "ECG", 1716579054020L);
        storage.addPatientData(1,  -0.4515193182467387, "ECG", 1716579055020L);
        storage.addPatientData(1,  0.18144082417659804, "ECG", 1716579056015L);
        storage.addPatientData(1,  0.7485106543877843, "ECG", 1716579057023L);

        // Generated alerts for patients
        alertGenerator.evaluateData(storage.getPatient(1));
        List<Alert> alerts = alertGenerator.getAlerts_Junit();

        // Expected alerts for patients
        List<Alert> alertsTests = new ArrayList<>();
         //  (none)

        // test if Expected alerts match with generated ones
        assertTrue(alertListsAreEqual(alerts,alertsTests), "Wrong diagnosis of not dangerous state");
    }
    /**
     *
     * ECG test
     * Healthy patient large dataset
     */
    @Test
    void  ECG_test_2() throws IOException {
        DataStorage storage = new DataStorage();
        FilesReader s = new FilesReader("src/test/java/alert_generation/ECG_test_2");
        s.readData(storage);
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // Generated alerts for patients
        alertGenerator.evaluateData(storage.getPatient(1));
        List<Alert> alerts = alertGenerator.getAlerts_Junit();

        // Expected alerts for patients
        List<Alert> alertsTests = new ArrayList<>();
        //  (none)

        // test if Expected alerts match with generated ones
        assertTrue(alertListsAreEqual(alerts,alertsTests), "Wrong diagnosis of not dangerous state or file wrongly read");
    }
    /**
     *
     * ECG test
     * Irregular beat pattern
     */
    @Test
    void  ECG_test_3() throws IOException {
        DataStorage storage = new DataStorage();
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        storage.addPatientData(1,  0.18144082417659804, "ECG", 1716579056015L);
        storage.addPatientData(1,  0.7485106543877843, "ECG", 1716579057023L);
        storage.addPatientData(1,  0.2485646543877843, "ECG", 1716579058025L);
        storage.addPatientData(1, -0.1592047543877843, "ECG", 1716579059027L);
        storage.addPatientData(1, 0.3485646543877843, "ECG", 1716579061027L);
        storage.addPatientData(1, 0.1585646543877843, "ECG", 1716579062028L);

        // Generated alerts for patients
        alertGenerator.evaluateData(storage.getPatient(1));
        List<Alert> alerts = alertGenerator.getAlerts_Junit();
        System.err.println(alerts.toString());
        // Expected alerts for patients
        List<Alert> alertsTests = new ArrayList<>();
        Alert alert = new Alert("1","Irregular Beat Pattern",1716579057023L);
        alertsTests.add(alert);


        // test if Expected alerts match with generated ones
        assertTrue(alertListsAreEqual(alerts,alertsTests), "no alert for Irregular Beat Pattern");
    }
    /**
     *
     * ECG test
     * Abnormal beat pattern
     */
    @Test
    void  ECG_test_4() throws IOException {
        DataStorage storage = new DataStorage();
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // Generated alerts for patients
        storage.addPatientData(1,  -0.07241039374407757, "ECG",  1716653765234L);
        storage.addPatientData(1,  -0.3384726671194705, "ECG",  1716653766734L);
        storage.addPatientData(1,  -0.2796054655371677, "ECG",  1716653768234L);
        storage.addPatientData(1, -0.11439213986568285, "ECG",  1716653769734L);
        storage.addPatientData(1,  0.1519569143952934, "ECG",  1716653771234L);
        storage.addPatientData(1, -0.2310429785511261, "ECG",  1716653772734L);
        storage.addPatientData(1, 0.7205593524089258, "ECG",  1716653774234L);
        storage.addPatientData(1, -0.5260688529517142, "ECG",  1716653775734L);
        storage.addPatientData(1,  -0.04508478904935673, "ECG",  1716653777234L);
        storage.addPatientData(1,  -0.044565856100253765, "ECG", 1716653778734L);

        alertGenerator.evaluateData(storage.getPatient(1));
        List<Alert> alerts = alertGenerator.getAlerts_Junit();

        // Expected alerts for patients
        List<Alert> alertsTests = new ArrayList<>();
        Alert alert = new Alert("1","Abnormal Heart Rate",1716653778734L);
        alertsTests.add(alert);


        // test if Expected alerts match with generated ones
        assertTrue(alertListsAreEqual(alerts,alertsTests));
    }

}





