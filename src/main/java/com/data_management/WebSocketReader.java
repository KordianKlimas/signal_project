package com.data_management;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import com.alerts.AlertGenerator;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;


/**
 * The WebSocketReader class extends WebSocketClient and implements DataReader to read and process data from a WebSocket server.
 */
public class WebSocketReader  extends WebSocketClient implements DataReader {
    private DataStorage dataStorage;


    /**
     * Constructor for WebSocketReader.
     *
     * @param serverURI The URI of the WebSocket server.
     */
    public WebSocketReader(URI serverURI, DataStorage dataStorage) {
        super(serverURI);
        this.dataStorage = dataStorage;
    }


    /**
     * Reads data and sets the DataStorage object.
     *
     * @param dataStorage The DataStorage object to store patient data.
     */
    @Override
    public void readData(DataStorage dataStorage)  {
        this.dataStorage = dataStorage;
        this.connect();
    }
    /**
     * Called when a new WebSocket connection is opened.
     *
     * @param handshakedata The handshake data of the WebSocket connection.
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("new connection opened");
    }
    /**
     * Called when the WebSocket connection is closed.
     *
     * @param code The exit code of the connection.
     * @param reason The reason for the connection closure.
     * @param remote Indicates whether the connection was closed remotely.
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed with exit code " + code + " additional info: " + reason);
    }

    /**
     * Called when a message is received from the WebSocket server.
     * Parses message to patient record and reads it into data storage
     *
     * @param message The received message as a String.
     */
    @Override
    public void onMessage(String message) {
        //System.out.println("received message: " + message);

        //adding Patient record to data storage;
        String[] parts = message.split("[\\s,]+"); // Split the message into parts based on whitespace or comma

        if(parts.length == 4){
            try {
                int patientId = Integer.parseInt(parts[0]);
                long timestamp = Long.parseLong(parts[1]);
                double measurement;
                // removes % from measurement value
                if (parts[3].contains("%")) {
                    parts[3] = parts[3].replace("%", "");
                }

                // checks for alerts
                if (parts[3].equals("resolved")) {
                    measurement = 0;
                } else if (parts[3].equals("triggered")) {
                    measurement = 1;
                } else {
                    measurement = Double.parseDouble(parts[3]);
                }

                this.dataStorage.addPatientData(patientId,measurement,parts[2],timestamp);
            } catch (NumberFormatException e) {
                System.err.println("unsupported message received (WebSocketReader: "+message);
            }

        }else{
            System.err.println("unsupported message received (WebSocketReader: "+message);
        }
    }
    /**
     * Called when a message in ByteBuffer format is received from the WebSocket server.
     *
     * @param message The received message as a ByteBuffer.
     */
    @Override
    public void onMessage(ByteBuffer message) {
        System.out.println("received ByteBuffer");
    }
    /**
     * Called when an error occurs in the WebSocket connection.
     *
     * @param ex The exception that occurred.
     */
    @Override
    public void onError(Exception ex) {
        System.err.println("an error occurred:" + ex);
        // Attempt reconnection on error
        try {
            this.connect();
        } catch (Exception e) {
            System.err.println("Reconnection attempt failed: " + e.getMessage());
        }
    }


    public static  void main(String[] args) throws  URISyntaxException {
        DataStorage s = new DataStorage();
        WebSocketReader w = new WebSocketReader(new URI("ws://localhost:8080"),s);
        w.readData(s);
        System.out.println("evaulate data:");
        AlertGenerator alertGenerator = new AlertGenerator(s);
        for(Patient patient : s.getAllPatients()){
            System.out.println("evaulate data:" +patient.getId());
            alertGenerator.evaluateData(patient);
        }
    }


}
