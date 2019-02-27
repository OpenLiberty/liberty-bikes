package org.libertybikes.player.service;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

public class PlayerTest {
    @Rule
    public GenericContainer playerContainer = new GenericContainer<>("libertybikes-player").withExposedPorts(8081);

    private String address;
    private Integer port;

    @Before
    public void setUp() {
        address = playerContainer.getContainerIpAddress();
        port = playerContainer.getFirstMappedPort();
    }

    @Test
    public void testRankDefaultAmount() throws Exception {
        URL playerURL = new URL("http", address, port, "/rank");
        HttpURLConnection conn = (HttpURLConnection) playerURL.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int maximumTries = 15;
        // The port connects before the app is ready, and returns a 404
        // so wait for a while until it changes, or give up
        for (int i = 0; i < maximumTries; i++) {
            System.out.println("(Attempt " + (i + 1) + "/" + maximumTries + ") Got response " + conn.getResponseCode() + " connecting to: " + conn);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                break;
            }
            conn.disconnect();

            Thread.sleep(1 * 1000);

            conn = (HttpURLConnection) playerURL.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
        }

        System.out.println("Got final response code: " + conn.getResponseCode());

        JsonReader jsonReader = Json.createReader(conn.getInputStream());
        JsonArray result = jsonReader.readArray();
        assertEquals(result.size(), 5);
    }

    @Test
    public void testRankCustomAmount() throws Exception {
        final int LIMIT = 10;
        URL playerURL = new URL("http", address, port, "/rank?limit=" + LIMIT);
        HttpURLConnection conn = (HttpURLConnection) playerURL.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int maximumTries = 15;
        // The port connects before the app is ready, and returns a 404
        // so wait for a while until it changes, or give up
        for (int i = 0; i < maximumTries; i++) {
            System.out.println("(Attempt " + (i + 1) + "/" + maximumTries + ") Got response " + conn.getResponseCode() + " connecting to: " + conn);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                break;
            }
            conn.disconnect();

            Thread.sleep(1 * 1000);

            conn = (HttpURLConnection) playerURL.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
        }

        System.out.println("Got final response code: " + conn.getResponseCode());

        JsonReader jsonReader = Json.createReader(conn.getInputStream());
        JsonArray result = jsonReader.readArray();
        assertEquals(result.size(), LIMIT);
    }
}