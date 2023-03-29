import java.io.*;
import java.net.*;

public class Receiver {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java Receiver <Network IP address> <receiverPort>");
            System.exit(1);
        }
        
        String networkIPAddress = args[0];
        int receiverPort = Integer.parseInt(args[1]);
        
        DatagramSocket receiverSocket = new DatagramSocket(receiverPort);
        
        int expectedSeqNum = 0;
        boolean endOfMessage = false;
        String receivedMessage = "";
        int numPacketsReceived = 0;
        int numPacketsCorrupted = 0;
        
        while (!endOfMessage) {
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            
            receiverSocket.receive(receivePacket);
            
            byte[] packetData = receivePacket.getData();
            int seqNum = packetData[0];
            int packetID = packetData[1];
            int checksum = ((packetData[2] & 0xFF) << 24) | ((packetData[3] & 0xFF) << 16) |
                            ((packetData[4] & 0xFF) << 8) | (packetData[5] & 0xFF);
            String packetContent = new String(packetData, 6, receivePacket.getLength() - 6);
            
            numPacketsReceived++;
            
            if (seqNum != expectedSeqNum) {
                System.out.println("Packet out of order, expected " + expectedSeqNum + " but received " + seqNum);
            } else {
                if (checksum == calculateChecksum(packetContent)) {
                    receivedMessage += packetContent + " ";
                    System.out.println("Received: " + packetID + ", " + packetContent);
                    expectedSeqNum = 1 - expectedSeqNum;
                } else {
                    System.out.println("Packet checksum error, discarding packet");
                    numPacketsCorrupted++;
                }
                
                if (packetContent.endsWith(".")) {
                    endOfMessage = true;
                    System.out.println("Message: " + receivedMessage);
                }
            }
            
            byte[] ackData = new byte[2];
            ackData[0] = (byte) expectedSeqNum;
            ackData[1] = (byte) 0;
            DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, 
                                                           InetAddress.getByName(networkIPAddress), 1234);
            receiverSocket.send(ackPacket);
            System.out.println("Sent ACK" + expectedSeqNum);
        }
        
        receiverSocket.close();
        
        System.out.println("Total number of packets received: " + numPacketsReceived);
        System.out.println("Total number of corrupted packets: " + numPacketsCorrupted);
    }
    
    private static int calculateChecksum(String data) {
        int checksum = 0;
        for (int i = 0; i < data.length(); i++) {
            checksum += (int) data.charAt(i);
        }
        return checksum;
    }

}
