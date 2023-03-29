import java.io.*;
import java.net.*;

public class Network {
    
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java Network <port>");
            System.exit(1);
        }
        
        int port = Integer.parseInt(args[0]);
        
        DatagramSocket senderSocket = new DatagramSocket();
        DatagramSocket receiverSocket = new DatagramSocket(port);
        
        int numPacketsSent = 0;
        int numPacketsDropped = 0;
        int numPacketsCorrupted = 0;
        
        System.out.println("Waiting for Sender and Receiver connections...");
        
        while (true) {
            byte[] receiveData = new byte[1024];
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
            
            if (Math.random() < 0.5) { // PASS
                byte[] sendData = new byte[2];
                sendData[0] = (byte) seqNum;
                sendData[1] = (byte) 0;
                
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, 
                                                                 senderAddress, senderPort);
                senderSocket.send(sendPacket);
                
                numPacketsSent++;
                System.out.println("Relayed: Packet" + seqNum + ", " + packetID + ", PASS");
            } else {
                if (Math.random() < 0.25) { // CORRUPT
                    checksum++;
                    numPacketsCorrupted++;
                    System.out.println("Relayed: Packet" + seqNum + ", " + packetID + ", CORRUPT");
                } else { // DROP
                    numPacketsDropped++;
                    System.out.println("Relayed: Packet" + seqNum + ", " + packetID + ", DROP");
                    continue;
                }
                
                byte[] sendData = new byte[1024];
                sendData[0] = (byte) seqNum;
                sendData[1] = (byte) 2; // ACK2
                sendData[2] = (byte) ((checksum >> 24) & 0xFF);
                sendData[3] = (byte) ((checksum >> 16) & 0xFF);
                sendData[4] = (byte) ((checksum >> 8) & 0xFF);
                sendData[5] = (byte) (checksum & 0xFF);
                
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, 
                                                                 senderAddress, senderPort);
                senderSocket.send(sendPacket);
            }
            
            if (numPacketsSent == 0 && numPacketsDropped == 0 && numPacketsCorrupted == 0) {
                System.out.println("Waiting for Sender and Receiver connections...");
            } else if (numPacketsSent > 0) {
                System.out.println("Sender connected");
            } else {
                System.out.println("Receiver connected");
            }
        }
    }
    
}
