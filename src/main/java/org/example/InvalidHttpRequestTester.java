package org.example;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InvalidHttpRequestTester {

    private static final String SERVER = "localhost";  // Server address
    private static final int PORT = 4567;              // Server port
    private static final int NUM_CLIENTS = 10;         // Number of concurrent invalid requests

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_CLIENTS); // Thread pool

        for (int i = 0; i < NUM_CLIENTS; i++) {
            int clientId = i; // Assign unique id to each client
            executor.submit(() -> {
                try {
                    sendInvalidRequest(clientId); // Simulate invalid client request
                } catch (IOException e) {
                    System.err.println("Client " + clientId + " encountered an error: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all clients to complete
        }
        System.out.println("Invalid HTTP request testing completed.");
    }

    private static void sendInvalidRequest(int clientId) throws IOException {
        try (Socket socket = new Socket(SERVER, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Create a list of invalid HTTP requests
            String[] invalidRequests = {
                    "INVALIDMETHOD / HTTP/1.1",                  // Unsupported HTTP method
                    "GET /unsupportedPage HTTP/1.1",             // Request for non-existent resource
                    "GET / HTTP/1.1\r\nHost: ",                  // Missing Host header
                    "POST / HTTP/1.1\r\nContent-Length: abc\r\n", // Malformed Content-Length
                    "PUT / HTTP/1.1\r\nContent-Type: text/xml",  // Unsupported Content-Type
                    "GET",                                        // Incomplete HTTP request
            };

            // Randomly pick an invalid request to send
            String invalidRequest = invalidRequests[clientId % invalidRequests.length];

            // Send the invalid request
            out.println(invalidRequest);
            out.println(); // End request with an empty line

            // Print the response from server
            String responseLine;
            System.out.println("Client " + clientId + " sent invalid request: " + invalidRequest);
            System.out.println("Client " + clientId + " received:");
            while ((responseLine = in.readLine()) != null) {
                System.out.println(responseLine);
            }

        } catch (IOException e) {
            throw new IOException("Error in client " + clientId + ": " + e.getMessage(), e);
        }
    }
}

