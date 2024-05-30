package data_management;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketReader;
import org.junit.Before;
import org.junit.Test;
import java.net.URI;
import java.net.URISyntaxException;
import static org.junit.Assert.*;

public class WebSocketReaderTest {
    private DataStorage mockStorage;
    private WebSocketReader client;

    @Before
    public void setUp() throws URISyntaxException {
        mockStorage = new DataStorage();
        client = new WebSocketReader(new URI("ws://localhost:8080"), mockStorage);
    }

    @Test
    public void testOnOpen() {
        client.onOpen(null);
        assertTrue(true); // Dummy assertion, as onOpen doesn't have any direct output
    }

    @Test
    public void testOnMessage_validMessage() {
        String message = "10,1714748468033,ECG,-0.34656395320945643";
        client.onMessage(message);
        PatientRecord record = mockStorage.getAllRecords(10).get(0);
        assertEquals(10, record.getPatientId());
        assertEquals(1714748468033L, record.getTimestamp());
        assertEquals("ECG", record.getRecordType());
        assertEquals(-0.34656395320945643, record.getMeasurementValue(), 0.01);
    }
    @Test
    public void testOnMessage_invalidMessageFormat() {
        String message = "Invalid message format";
        client.onMessage(message);
        assertTrue(mockStorage.getAllRecords(10).isEmpty());
    }

    @Test
    public void testOnClose() {
        client.onClose(1000, "Normal closure", false);
        assertFalse(client.isOpen());
    }

    @Test
    public void testOnError() {
        client.onError(new RuntimeException("Test error"));
        assertTrue(true); // Dummy assertion, as onError doesn't have any direct output
    }
}