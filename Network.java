import java.io.*;
import java.net.*;

public class Network {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java Network <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        DatagramSocket socket = new DatagramSocket(port);

        System.out.println("Waiting for sender and receiver to connect...");

        InetAddress senderIPAddress = null;
        int senderPort = -1;
        InetAddress receiverIPAddress = null;
        int receiverPort = -1;

        while (true) {
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            String packetContent = new String(receivePacket.getData(), 0, receivePacket.getLength());

            if (packetContent.equals("CONNECT")) {
                if (senderIPAddress == null) {
                    senderIPAddress = receivePacket.getAddress();
                    senderPort = receivePacket.getPort();
                    System.out.println("Sender connected at " + senderIPAddress.getHostAddress() + ":" + senderPort);
                } else if (receiverIPAddress == null) {
                    receiverIPAddress = receivePacket.getAddress();
                    receiverPort = receivePacket.getPort();
                    System.out.println("Receiver connected at " + receiverIPAddress.getHostAddress() + ":" + receiverPort);
                }

                if (senderIPAddress != null && receiverIPAddress != null) {
                    System.out.println("Sender and receiver are now connected. Ready to transfer messages.");
                    break;
                }
            }
        }

        while (true) {
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            String packetContent = new String(receivePacket.getData(), 0, receivePacket.getLength());
            InetAddress sourceIPAddress = receivePacket.getAddress();
            int sourcePort = receivePacket.getPort();

            if (sourceIPAddress.equals(senderIPAddress) && sourcePort == senderPort) {
                System.out.println("Received packet from sender: " + packetContent);
                DatagramPacket forwardPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
                        receiverIPAddress, receiverPort);
                socket.send(forwardPacket);
                System.out.println("Forwarded packet to receiver: " + packetContent);
            } else if (sourceIPAddress.equals(receiverIPAddress) && sourcePort == receiverPort) {
                System.out.println("Received packet from receiver: " + packetContent);
                DatagramPacket forwardPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
                        senderIPAddress, senderPort);
                socket.send(forwardPacket);
                System.out.println("Forwarded packet to sender: " + packetContent);
            }
        }
    }
}
