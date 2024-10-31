package org.example;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientLoadTester {

    private static final String SERVER = "localhost";  // Server address
    private static final int PORT = 4567;              // Server port
    private static final int NUM_CLIENTS = 50;         // Number of concurrent clients
    private static final String REQUEST_TYPE = "PUT";  // Change to "GET" for GET request load testing

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_CLIENTS); // Thread pool

        for (int i = 0; i < NUM_CLIENTS; i++) {
            int clientId = i; // Assign unique id to each client
            executor.submit(() -> {
                try {
                    sendRequest(clientId); // Simulate client request
                } catch (IOException e) {
                    System.err.println("Client " + clientId + " encountered an error: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all clients to complete
        }
        System.out.println("Client load testing completed.");
    }

    private static void sendRequest(int clientId) throws IOException {
        try (Socket socket = new Socket(SERVER, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            if (REQUEST_TYPE.equalsIgnoreCase("PUT")) {
                String jsonBody = "{\"temp\": \"25\"}";
                out.println("PUT /weather.json HTTP/1.1");
                out.println("Host: " + SERVER);
                out.println("Content-Type: application/json");
                out.println("Content-Length: " + jsonBody.length());
                out.println();
                out.println(jsonBody);
            } else if (REQUEST_TYPE.equalsIgnoreCase("GET")) {
                out.println("GET /weather.json HTTP/1.1");
                out.println("Host: " + SERVER);
                out.println();
            }

            // Print the response from server
            String responseLine;
            System.out.println("Client " + clientId + " received:");
            while ((responseLine = in.readLine()) != null) {
                System.out.println(responseLine);
            }

        } catch (IOException e) {
            throw new IOException("Error in client " + clientId + ": " + e.getMessage(), e);
        }
    }
}
