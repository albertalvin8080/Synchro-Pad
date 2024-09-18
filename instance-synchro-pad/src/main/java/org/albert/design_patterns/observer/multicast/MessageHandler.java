package org.albert.design_patterns.observer.multicast;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.util.UUID;

public class MessageHandler
{
    private final MulticastSocket multicastSocket;
    private final InetAddress mcastaddr;
    private final UUID uuid;
    private final JTextArea textArea;

    public MessageHandler(MulticastSocket multicastSocket, InetAddress mcastaddr, UUID uuid, JTextArea textArea)
    {
        this.multicastSocket = multicastSocket;
        this.mcastaddr = mcastaddr;
        this.uuid = uuid;
        this.textArea = textArea;
    }

    public DatagramPacket constructPackage(String senderId, short operationType, int offset, int length, String text)
    {
        byte[] msg = new StringBuilder(senderId)
                .append(":").append(operationType)
                .append(":").append(offset)
                .append(":").append(length)
                .append(":").append(text)
                .toString()
                .getBytes();

        return new DatagramPacket(msg, msg.length, mcastaddr, 1234);
    }

    public void sendMessage(short operationType, int offset, int length, String text)
    {
        DatagramPacket dOut = constructPackage(uuid.toString(), operationType, offset, length, text);
        try
        {
            multicastSocket.send(dOut);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public DatagramPacket receiveMessage()
    {
        byte[] buf = new byte[5000];
        DatagramPacket dIn = new DatagramPacket(buf, buf.length);
        try
        {
            multicastSocket.receive(dIn);
            return dIn;
        }
        catch (SocketException e)
        {
            if (e.getMessage().equals("Socket closed"))
                System.out.println("Connection closed");
            else
                e.printStackTrace();
            return null;
        }
        catch (SocketTimeoutException e)
        {
            System.out.println("Timeout reached, trying again...");
            return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public String[] extractMessage(DatagramPacket packet)
    {
        String str = new String(packet.getData(), 0, packet.getLength());
        final String[] split = str.split(":", 5);
        return split;
    }

    public void processMessage(short operationType, int offset, int length, String text)
    {
//        System.out.println("DATA SHARER");
//        System.out.println(Arrays.toString(
//                new Object[]{offset, length, text, operationType}
//        ));

        final StringBuilder sb = new StringBuilder(textArea.getText());
        if (operationType == DataSharerMulticast.OP_INSERT ||
                operationType == DataSharerMulticast.OP_DELETE)
        {
            sb.replace(offset, offset + length, text.equals("null") ? "" : text);
            textArea.setText(sb.toString());
        }
    }
}
