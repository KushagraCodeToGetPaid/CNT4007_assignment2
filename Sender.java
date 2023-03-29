import java.io.*;
import java.net.*;

public class Sender {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Usage: java Sender <URL> <portNumber> <MessageFileName>");
            System.exit(1);
        }

        String url = args[0];
        int portNumber = Integer.parseInt(args[1]);
        String messageFileName = args[2];

        BufferedReader messageReader = new BufferedReader(new FileReader(messageFileName));
        StringBuilder messageBuilder = new StringBuilder();
        String line;
        while ((line = messageReader.readLine()) != null) {
            messageBuilder.append(line);
        }
        messageReader.close();
        String message = messageBuilder.toString();

        DatagramSocket senderSocket = new DatagramSocket();

        int sequenceNumber = 0;
        int packetID = 0;
        int packetSize = 1024;

        while (message.length() > 0) {
            String packetContent;
            if (message.length() <= packetSize) {
                packetContent = message;
                message = "";
            } else {
                packetContent = message.substring(0, packetSize);
                message = message.substring(packetSize);
            }
            byte[] packetData = createPacketData(sequenceNumber, packetID, packetContent);
            DatagramPacket packet = new DatagramPacket(packetData, packetData.length, InetAddress.getByName(url), portNumber);
            senderSocket.send(packet);
            System.out.println("Sent: " + packetID + ", " + packetContent);
            sequenceNumber = 1 - sequenceNumber;
            packetID++;
        }

        senderSocket.close();
    }

    private static byte[] createPacketData(int seqNum, int packetID, String content) {
        byte[] packetData = new byte[6 + content.length()];
        packetData[0] = (byte) seqNum;
        packetData[1] = (byte) packetID;
        int checksum = calculateChecksum(content);
        packetData[2] = (byte) ((checksum >> 24) & 0xFF);
        packetData[3] = (byte) ((checksum >> 16) & 0xFF);
        packetData[4] = (byte) ((checksum >> 8) & 0xFF);
        packetData[5] = (byte) (checksum & 0xFF);
        System.arraycopy(content.getBytes(), 0, packetData, 6, content.length());
        return packetData;
    }

    private static int calculateChecksum(String data) {
        int checksum = 0;
        for (int i = 0; i < data.length(); i++) {
            checksum += (int) data.charAt(i);
        }
        return checksum;
    }

}
