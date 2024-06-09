package data_management;

import static org.junit.Assert.*;

import com.data_management.DataStorage;
import com.data_management.FilesReader;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.Test;
import java.io.IOException;
import java.util.*;

public class FilesReaderTest {
    // Helper method for Alert check
    private boolean recordsListsAreEqual(List<PatientRecord> list_1, List<PatientRecord> list_2) {
        if (list_1.size() != list_2.size()) {
            return false;
        }

        for (int i = 0; i < list_1.size(); i++) {
            if (list_1.get(i).getTimestamp() != list_2.get(i).getTimestamp() ||
                    !list_1.get(i).getRecordType().equals(list_2.get(i).getRecordType()) ||
                    !list_1.get(i).getPatientId().equals(list_2.get(i).getPatientId())) {
                return false; // Return false if any field is not equal
            }
        }

        return true; // Return true if all elements are equal
    }
    @Test
    public void testReadData() {
        // Create a mock directory with test files
        String testDirectory = "src/test/java/data_management/testFiles";

        // Create a mock DataStorage
        DataStorage dataStorage = new DataStorage();

        // Initialize the FilesReader with the test directory
        FilesReader filesReader = new FilesReader(testDirectory);

        try {
            // Call the readData method to read test data into the DataStorage
            filesReader.readData(dataStorage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<PatientRecord> recordsFromFiles = new ArrayList<>(); // Use ArrayList<>
        List<PatientRecord> recordsTest = new ArrayList<>(); // Use ArrayList<>

        for (Patient patient : dataStorage.getAllPatients()) {
            recordsFromFiles.addAll(patient.getAllRecords());
        }

        recordsTest.add(new PatientRecord("81", 194.74342822136995, "Cholesterol", 1717097371814L));
        recordsTest.add(new PatientRecord("63", 163.11083683653274, "Cholesterol", 1717097371812L));
        recordsTest.add(new PatientRecord("42", 1, "Alert", 1717097371869L));
        recordsTest.add(new PatientRecord("15", 1, "Alert", 1717097372795L));
        recordsTest.add(new PatientRecord("39", 83.0, "DiastolicPressure", 1717097371846L));
        recordsTest.add(new PatientRecord("66", 79.0, "DiastolicPressure", 1717097371846L));
        recordsTest.add(new PatientRecord("55", -0.21531770140330428, "ECG", 1717097371797L));
        recordsTest.add(new PatientRecord("62", -0.34126031661753264, "ECG", 1717097371813L));
        recordsTest.add(new PatientRecord("59", 5.676784759217333, "RedBloodCells", 1717097371849L));
        recordsTest.add(new PatientRecord("63", 5.734493484478322, "RedBloodCells", 1717097371849L));
        recordsTest.add(new PatientRecord("50", 95.0, "Saturation", 1717097371841L));
        recordsTest.add(new PatientRecord("93", 96.0, "Saturation", 1717097371815L));
        recordsTest.add(new PatientRecord("66", 110.0, "SystolicPressure", 1717097371798L));
        recordsTest.add(new PatientRecord("39", 120.0, "SystolicPressure", 1717097371807L));
        recordsTest.add(new PatientRecord("59", 8.408776292155478, "WhiteBloodCells", 1717097371847L));
        recordsTest.add(new PatientRecord("63", 9.559679464627013, "WhiteBloodCells", 1717097371847L));

        recordsTest.sort(Comparator.comparing(PatientRecord::getPatientId)
                .thenComparing(PatientRecord::getRecordType));
        recordsFromFiles.sort(Comparator.comparing(PatientRecord::getPatientId)
                .thenComparing(PatientRecord::getRecordType));


        assertTrue(recordsListsAreEqual(recordsTest,recordsFromFiles));


    }


}
