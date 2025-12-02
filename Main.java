import java.util.LinkedList;
import java.util.HashSet;
import java.util.Iterator;

// Packet class representing a network packet
class Packet {
    private int sequenceNumber;
    private String data;
    
    public Packet(int sequenceNumber, String data) {
        this.sequenceNumber = sequenceNumber;
        this.data = data;
    }
    
    public int getSequenceNumber() {
        return sequenceNumber;
    }
    
    public String getData() {
        return data;
    }
    
    @Override
    public String toString() {
        return "Packet[" + sequenceNumber + ": " + data + "]";
    }
}

// PacketReceiver class to manage received packets and reconstruct message
class PacketReceiver {
    private LinkedList<Packet> buffer;
    private int expectedSeq;
    private StringBuilder message;
    private HashSet<Integer> processedPackets;
    private int totalPackets;
    private boolean[] receivedFlags;
    
    public PacketReceiver() {
        buffer = new LinkedList<>();
        expectedSeq = 0;
        message = new StringBuilder();
        processedPackets = new HashSet<>();
        totalPackets = -1;
    }
    
    public PacketReceiver(int totalPackets) {
        this();
        this.totalPackets = totalPackets;
        if (totalPackets > 0) {
            receivedFlags = new boolean[totalPackets];
        }
    }
    
    public void receivePacket(Packet packet) {
        int seqNum = packet.getSequenceNumber();
        
        System.out.println("\n--- Receiving Packet " + seqNum + ": \"" + packet.getData() + "\" ---");
        
        // Check for duplicate
        if (processedPackets.contains(seqNum)) {
            System.out.println(" Ignoring duplicate packet: " + seqNum);
            return;
        }
        
        // Mark as received if tracking is enabled
        if (receivedFlags != null && seqNum >= 0 && seqNum < totalPackets) {
            receivedFlags[seqNum] = true;
        }
        
        // Insert packet in sorted order in the buffer
        insertInOrder(packet);
        
        // Display current buffer
        displayBuffer();
        
        // Try to process the buffer
        processBuffer();
        
        // Check for missing packets
        if (totalPackets > 0) {
            checkForMissingPackets();
        }
    }
    
    private void insertInOrder(Packet packet) {
        int index = 0;
        for (Packet p : buffer) {
            if (p.getSequenceNumber() > packet.getSequenceNumber()) {
                break;
            }
            index++;
        }
        buffer.add(index, packet);
    }
    
    private void processBuffer() {
        boolean processedAny = false;
        
        while (!buffer.isEmpty()) {
            Packet firstPacket = buffer.getFirst();
            
            if (firstPacket.getSequenceNumber() == expectedSeq) {
                // Process this packet
                buffer.removeFirst();
                message.append(firstPacket.getData()).append(" ");
                processedPackets.add(expectedSeq);
                
                System.out.println("Processed packet " + firstPacket.getSequenceNumber() + 
                                  " (\"" + firstPacket.getData() + "\")");
                System.out.println("Current message: \"" + getMessage() + "\"");
                
                expectedSeq++;
                processedAny = true;
            } else {
                break;
            }
        }
        
        if (processedAny) {
            System.out.println("Next expected sequence: " + expectedSeq);
        }
    }
    
    private void checkForMissingPackets() {
        System.out.print("Packet Status: ");
        int displayLimit = Math.min(expectedSeq + 5, totalPackets);
        for (int i = 0; i < displayLimit; i++) {
            if (i < expectedSeq) {
                System.out.print(i + "✓ ");
            } else if (receivedFlags != null && receivedFlags[i]) {
                System.out.print(i + "✓ ");
            } else {
                System.out.print(i + "✗ ");
            }
        }
        System.out.println();
    }
    
    public String getMessage() {
        return message.toString().trim();
    }
    
    public int getExpectedSequenceNumber() {
        return expectedSeq;
    }
    
    public int getBufferSize() {
        return buffer.size();
    }
    
    public void simulateTimeout() {
        System.out.println("\nTIMEOUT OCCURRED!");
        System.out.println(" Expected packet " + expectedSeq + " is missing.");
        System.out.println(" Requesting retransmission of packet " + expectedSeq);
    }
    
    public void displayBuffer() {
        System.out.print(" Buffer contents [");
        for (Packet p : buffer) {
            System.out.print(p.getSequenceNumber() + " ");
        }
        System.out.println("]");
    }
    
    public void displaySummary() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(" FINAL SUMMARY");
        System.out.println("=".repeat(50));
        System.out.println("Reconstructed Message: \"" + getMessage() + "\"");
        System.out.println(" Next Expected Sequence: " + expectedSeq);
        System.out.println(" Packets in Buffer: " + getBufferSize());
        System.out.println(" Total Packets Processed: " + processedPackets.size());
        System.out.println("=".repeat(50));
    }
}

// Main class with all test scenarios
public class Main {
    public static void main(String[] args) {
        System.out.println("=".repeat(50));
        System.out.println(" PACKET NETWORK MESSAGE RECEIVER SIMULATION");
        System.out.println("=".repeat(50));
        
        // Test 1: Basic Scenario
        System.out.println("\n" + "=".repeat(50));
        System.out.println("TEST 1: BASIC SCENARIO (Packets: 2→0→1)");
        System.out.println("=".repeat(50));
        testBasicScenario();
        
        // Test 2: Duplicate Handling
        System.out.println("\n" + "=".repeat(50));
        System.out.println("TEST 2: DUPLICATE PACKET HANDLING");
        System.out.println("=".repeat(50));
        testWithDuplicates();
        
        // Test 3: Missing Packets
        System.out.println("\n" + "=".repeat(50));
        System.out.println("TEST 3: MISSING PACKETS WITH TIMEOUT");
        System.out.println("=".repeat(50));
        testWithMissingPackets();
        
        // Test 4: Complex Scenario
        System.out.println("\n" + "=".repeat(50));
        System.out.println("TEST 4: COMPLEX OUT-OF-ORDER DELIVERY");
        System.out.println("=".repeat(50));
        testComplexScenario();
    }
    
    private static void testBasicScenario() {
        PacketReceiver receiver = new PacketReceiver();
        
        System.out.println(" Sending packets in order: 2, 0, 1");
        
        receiver.receivePacket(new Packet(2, "!"));
        receiver.receivePacket(new Packet(0, "Hello"));
        receiver.receivePacket(new Packet(1, "World"));
        
        receiver.displaySummary();
    }
    
    private static void testWithDuplicates() {
        PacketReceiver receiver = new PacketReceiver();
        
        System.out.println(" Sending packets: 0, 2, 1, 0(dup), 1(dup)");
        
        receiver.receivePacket(new Packet(0, "Hello"));
        receiver.receivePacket(new Packet(2, "!"));
        receiver.receivePacket(new Packet(1, "World"));
        receiver.receivePacket(new Packet(0, "Hello")); // Duplicate
        receiver.receivePacket(new Packet(1, "World")); // Duplicate
        
        receiver.displaySummary();
    }
    
    private static void testWithMissingPackets() {
        PacketReceiver receiver = new PacketReceiver(6);
        
        System.out.println(" Sending packets: 4, 0, 2 (Missing: 1, 3, 5)");
        
        receiver.receivePacket(new Packet(4, "last"));
        receiver.receivePacket(new Packet(0, "This"));
        receiver.receivePacket(new Packet(2, "test"));
        
        // Simulate timeout
        receiver.simulateTimeout();
        
        // Receive missing packets
        System.out.println("\n Receiving missing packets...");
        receiver.receivePacket(new Packet(1, "is"));
        receiver.receivePacket(new Packet(3, "a"));
        receiver.receivePacket(new Packet(5, "message"));
        
        receiver.displaySummary();
    }
    
    private static void testComplexScenario() {
        PacketReceiver receiver = new PacketReceiver();
        
        System.out.println(" Sending 9 packets in random order:");
        
        // Create a sentence split into 9 packets
        String[] words = {"The", "quick", "brown", "fox", "jumps", 
                          "over", "the", "lazy", "dog"};
        
        // Deliver in extremely random order
        int[] randomOrder = {8, 2, 5, 0, 7, 1, 6, 3, 4};
        
        for (int i = 0; i < randomOrder.length; i++) {
            int seq = randomOrder[i];
            receiver.receivePacket(new Packet(seq, words[seq]));
        }
        
        receiver.displaySummary();
    }
}