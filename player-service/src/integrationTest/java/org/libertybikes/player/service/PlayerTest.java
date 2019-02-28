package org.libertybikes.player.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import io.openliberty.testcontainers.LibertyContainer;

public class PlayerTest {

    static Logger LOGGER = LoggerFactory.getLogger(PlayerTest.class);

    static {
        BasicConfigurator.configure();
    }

    @ClassRule
    public static LibertyContainer playerContainer = new LibertyContainer("libertybikes-player")
                    .withExposedPorts(8081)
                    .waitForMPHealth();

    private String address;
    private Integer port;

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void setupClass() {
        playerContainer.followOutput(new Slf4jLogConsumer(LOGGER));
    }

    @Before
    public void setUp() {
        System.out.println("BEGIN TEST: " + testName.getMethodName());
        address = playerContainer.getContainerIpAddress();
        port = playerContainer.getFirstMappedPort();
    }

    @Test
    public void testRankDefaultAmount() throws Exception {
        URL playerURL = new URL("http", address, port, "/rank");
        HttpURLConnection conn = (HttpURLConnection) playerURL.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        System.out.println("Got response " + conn.getResponseCode() + " connecting to: " + conn);
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            fail("Got HTTP response: " + conn.getResponseCode());
        }
        System.out.println("Got final response code: " + conn.getResponseCode());

        JsonReader jsonReader = Json.createReader(conn.getInputStream());
        JsonArray result = jsonReader.readArray();
        assertEquals(result.size(), 5);

        conn.disconnect();
    }

    @Test
    public void testRankCustomAmount() throws Exception {
        final int LIMIT = 10;
        URL playerURL = new URL("http", address, port, "/rank?limit=" + LIMIT);
        HttpURLConnection conn = (HttpURLConnection) playerURL.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        System.out.println("Got response " + conn.getResponseCode() + " connecting to: " + conn);
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            fail("Got HTTP response: " + conn.getResponseCode());
        }
        System.out.println("Got final response code: " + conn.getResponseCode());

        JsonReader jsonReader = Json.createReader(conn.getInputStream());
        JsonArray result = jsonReader.readArray();
        assertEquals(result.size(), LIMIT);

        conn.disconnect();
    }
}