package com.data_management;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;


public class WebSocketReader  implements DataReader {

    @Override
    public void readData(DataStorage dataStorage) throws IOException, URISyntaxException {
        WebSocketClient client = new EmptyClient(new URI("ws://localhost:8080")); // Update the URI here
        client.connect();

    }
    public static  void main(String[] args) throws IOException, URISyntaxException {
        DataStorage s = new DataStorage();
        WebSocketReader w = new WebSocketReader();
        w.readData(s);

        
    }


}
