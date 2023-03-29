import java.io.*;
import java.net.*;

public class Receiver {
    
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java Receiver <networkAddress> <networkPort>");
            System.exit(1);
        }
        
        String networkAddress = args[0];
        int networkPort = Integer.parseInt(args[1]);
        
        DatagramSocket receiverSocket = new DatagramSocket();
        
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1];
        int expectedSeqNum = 0;
        int numPacketsReceived = 0;
        
        System.out.println("Trying to connect to network at " + networkAddress + ":" + networkPort);
        while (true) {
            try {
                InetAddress networkInetAddress = InetAddress.getByName(networkAddress);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, 
                                                                 networkInetAddress, networkPort);
                receiverSocket.send(sendPacket);
                System.out.println("Sent connection request to network");
                break;
            } catch (Exception e) {
                Thread.sleep(1000); // wait 1 second and try again
            }
        }
        System.out.println("Successfully connected to network");
        
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            receiverSocket.receive(receivePacket);
            
            InetAddress senderAddress = receivePacket.getAddress();
            int senderPort = receivePacket.getPort();
            byte[] packetData = receivePacket.getData();
            int seqNum = packetData[0];
            int packetID = packetData[1];
            int checksum = ((packetData[2] & 0xFF) << 24) | ((packetData[3] & 0xFF) << 16) |
                            ((packetData[4] & 0xFF) << 8) | (packetData[5] & 0xFF);
            String packetContent = new String(packetData, 6, receivePacket.getLength() - 6);
            
            if (seqNum == expectedSeqNum && checksum == packetContent.chars().sum()) {
                System.out.println("Received: Packet" + seqNum + ", " + packetID + ", PASS");
                expectedSeqNum = (expectedSeqNum + 1) % 2;
                numPacketsReceived++;
                
                if (packetContent.endsWith(".")) {
                    String message = packetContent.substring(0, packetContent.length() - 1).replace(" ", " ");
                    System.out.println("Message: " + message);
                }
                
                byte[] ackData = new byte[2];
                ackData[0] = (byte) expectedSeqNum;
                ackData[1] = (byte) 0;
                
                DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, 
                                                               senderAddress, senderPort);
                receiverSocket.send(ackPacket);
            } else {
                System.out.println("Received: Packet" + seqNum + ", " + packetID + ", CORRUPT");
            }
        }
    }
    
}
