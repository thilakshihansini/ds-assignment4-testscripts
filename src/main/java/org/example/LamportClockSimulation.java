package org.example;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LamportClockSimulation {

    private static final int NUM_PROCESSES = 3;
    private static final int NUM_EVENTS = 10;
    private static final ExecutorService executor = Executors.newFixedThreadPool(NUM_PROCESSES);

    public static void main(String[] args) throws InterruptedException {
        Process[] processes = new Process[NUM_PROCESSES];

        // Initialize processes
        for (int i = 0; i < NUM_PROCESSES; i++) {
            processes[i] = new Process(i);
        }

        // Simulate message exchanges
        for (int i = 0; i < NUM_EVENTS; i++) {
            int senderIndex = ThreadLocalRandom.current().nextInt(NUM_PROCESSES);
            int receiverIndex;
            do {
                receiverIndex = ThreadLocalRandom.current().nextInt(NUM_PROCESSES);
            } while (senderIndex == receiverIndex);  // Ensure sender and receiver are different

            Process sender = processes[senderIndex];
            Process receiver = processes[receiverIndex];

            // Send message asynchronously
            executor.submit(() -> sender.sendMessage(receiver));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("\nFinal Lamport Clock Values:");
        for (Process process : processes) {
            System.out.println("Process " + process.id + " Clock: " + process.getClock());
        }
    }
}

class Process {

    private final AtomicInteger lamportClock = new AtomicInteger(0); // Lamport Clock
    public final int id;  // Process ID

    public Process(int id) {
        this.id = id;
    }

    public int getClock() {
        return lamportClock.get();
    }

    // Increment Lamport clock and send a message
    public void sendMessage(Process receiver) {
        int timestamp = incrementClock();
        System.out.println("Process " + id + " sending message to Process " + receiver.id + " with timestamp " + timestamp);
        receiver.receiveMessage(timestamp);
    }

    // Receive a message and adjust Lamport clock accordingly
    public void receiveMessage(int receivedTimestamp) {
        int currentClock = lamportClock.get();
        lamportClock.set(Math.max(currentClock, receivedTimestamp) + 1);
        System.out.println("Process " + id + " received message with timestamp " + receivedTimestamp +
                ", updated clock to " + lamportClock.get());
    }

    // Increment the Lamport clock (for local events or sending messages)
    public synchronized int incrementClock() {
        return lamportClock.incrementAndGet();
    }
}

