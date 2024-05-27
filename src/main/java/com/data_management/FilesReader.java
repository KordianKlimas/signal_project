package com.data_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Reads all the files in a directory and forwards their data to dataStorage.
 * Assumes that files are in .txt format and that they share names with FileOutputStrategy Files:
 * <ul>
 * <li>Cholesterol.txt</li>
 * <li>DiastolicPressure.txt</li>
 * <li>ECG.txt</li>
 * <li>RedBloodCells.txt</li>
 * <li>Saturation.txt</li>
 * <li>SystolicPressure.txt</li>
 * <li>WhiteBloodCells.txt</li>
 * </ul>
 */

public class FilesReader implements DataReader {
    String Base_directory;

    /**
     * Allows to import data from files in the Base_directory  directory to specified dataStorage
     * @param Base_directory
     */
    public FilesReader(String Base_directory){
        this.Base_directory = Base_directory;
    }

    /**
     * Allows change of directory where files with data are located
     * @param file_directory
     */
    public void ChangeDirectory(String file_directory){
        this.Base_directory = Base_directory;
    }
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        File directory = new File(Base_directory);
        if (!directory.isDirectory()) {
            throw new IOException("Base directory is not a valid directory.");
        }

        // The List<String>  and switch could be replaced with different more scalable approach that would allow to create new types of
        // record types beside the 7 that are currently supported ( Cholesterol, ECG.....)
        // The type would be changed from String to Object, named ex.MetricType. MetricTypes could be then
        // edited by staff, new types of measurements could be added, making app scalable.

        // Define the list of files to parse
        List<String> filesToParse = Arrays.asList(
                "Cholesterol.txt",
                "DiastolicPressure.txt",
                "ECG.txt",
                "RedBloodCells.txt",
                "Saturation.txt",
                "SystolicPressure.txt",
                "WhiteBloodCells.txt",
                "Alert.txt"
        );

        // Filter files based on the list
        File[] files = directory.listFiles((dir, name) -> filesToParse.contains(name));
        if (files == null) {
            throw new IOException("Error listing files in directory.");
        }

        for (File file : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(", ");

                    if (parts.length == 4) {
                        int patientId = Integer.parseInt(parts[0].split(": ")[1]);
                        long timestamp = Long.parseLong(parts[1].split(": ")[1].trim());
                        String label = parts[2].split(": ")[1];

                        double measurementValue = getMeasurementValue(parts);
                        // Add data to dataStorage
                        dataStorage.addPatientData(patientId, measurementValue, label, timestamp);
                    }
                }
            }
        }
    }

    /**
     * Parsec measurement data according to the label.
     * @param parts Line of data from file
     * @return measurementValue
     */
    private static double getMeasurementValue(String[] parts) {
        String dataString = parts[3].split(": ")[1];
        String label = parts[2].split(": ")[1];
        double measurementValue;
        switch (label) {
            case "Saturation":
                measurementValue = Double.parseDouble(dataString.replace("%", ""));
                break;
            case "Alert":
                if(dataString.equals("triggered"  )){
                    measurementValue =1;
                }else{
                    measurementValue =0;
                }
                break;
            default:
                measurementValue = Double.parseDouble(dataString);
                break;
        }

        return measurementValue;
    }

    public static void main(String[] args) {
        try {
            FilesReader s = new FilesReader("C:\\Users\\kordi\\IdeaProjects\\signal_project\\src\\test\\java\\data_management\\testFiles");
            DataStorage storage = new DataStorage();

            s.readData(storage);
     //       for(PatientRecord re : storage.getAllRecords(1)){
     //           System.out.println(re.getRecordType());
     //       }


        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

}