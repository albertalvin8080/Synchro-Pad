package org.albert.util;

import javax.swing.*;
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.net.*;
import java.util.UUID;

public class DataSharer
{
    public static final short OP_BREAK = -1;
    public static final short OP_NEW_REQUEST = 10;
    public static final short OP_NEW_RESPONSE = 11;
    public static final short OP_INSERT = 1;
    public static final short OP_DELETE = 2;

    private final JTextArea textArea;
    private final UUID uuid;
    private InetAddress mcastaddr;
    private InetSocketAddress group;
    private NetworkInterface netIf;
    private MulticastSocket multicastSocket;
    private final Cleaner.Cleanable cleanable;
    private final Thread thread;

    public DataSharer(JTextArea textArea) throws IOException, InterruptedException
    {
        this.textArea = textArea;
        this.uuid = UUID.randomUUID();
        initializeNetworking();
        cleanable = Cleaner.create().register(this, () -> {
            try {
                multicastSocket.leaveGroup(group, netIf);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (multicastSocket != null) {
                    multicastSocket.close();
                }
            }
        });

        final Thread init = init();
        init.start();
        System.out.println("Waiting for init...");
        init.join();
        System.out.println("Init finished.");

        thread = createAsyncReceiveThread();
        thread.start();
    }

    private void initializeNetworking() throws IOException
    {
        mcastaddr = InetAddress.getByName("229.1.1.1");
        netIf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        group = new InetSocketAddress(mcastaddr, 1234);
        multicastSocket = new MulticastSocket(1234);
        multicastSocket.joinGroup(group, netIf);
    }

    private Thread init()
    {
        return new Thread(() -> {
            sendMessage(OP_NEW_REQUEST, 0, 0, "");

            while (true)
            {
                DatagramPacket dIn = receiveMessage();
                String[] parts = extractMessage(dIn);
                String senderId = parts[0];
                short operationType = Short.parseShort(parts[1]);
                String text = parts[4];

                if (senderId.equals(this.uuid.toString()) && operationType == DataSharer.OP_NEW_RESPONSE)
                {
                    textArea.setText(text);
                    break;
                }
            }
        });
    }

    public void share(int offset, int length, String text, short operationType)
    {
        sendMessage(operationType, offset, length, text);
    }

    private Thread createAsyncReceiveThread()
    {
        return new Thread(() -> {
            System.out.println("Thread listening...");
            while (true)
            {
                DatagramPacket dIn = receiveMessage();
                if (dIn != null)
                {
                    String[] parts = extractMessage(dIn);
                    String senderId = parts[0];
                    short operationType = Short.parseShort(parts[1]);
                    int offset = Integer.parseInt(parts[2]);
                    int length = Integer.parseInt(parts[3]);
                    String text = parts[4];

                    if (!senderId.equals(uuid.toString()))
                    {
                        processMessage(operationType, offset, length, text);
                    }
                }
            }
        });
    }

    private void sendMessage(short operationType, int offset, int length, String text)
    {
        StringBuilder sb = new StringBuilder(uuid.toString())
                .append(":").append(operationType)
                .append(":").append(offset)
                .append(":").append(length)
                .append(":").append(text);

        byte[] msg = sb.toString().getBytes();
        DatagramPacket dOut = new DatagramPacket(msg, msg.length, mcastaddr, 1234);
        try
        {
            multicastSocket.send(dOut);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private DatagramPacket receiveMessage()
    {
        byte[] buf = new byte[1000];
        DatagramPacket dIn = new DatagramPacket(buf, buf.length);
        try
        {
            multicastSocket.receive(dIn);
            return dIn;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private String[] extractMessage(DatagramPacket packet)
    {
        String str = new String(packet.getData(), 0, packet.getLength());
        return str.split(":", 5);
    }

    private void processMessage(short operationType, int offset, int length, String text)
    {
        final StringBuilder sb = new StringBuilder(textArea.getText());
        if (operationType == DataSharer.OP_INSERT)
        {
            sb.insert(offset, text);
        }
        else if (operationType == DataSharer.OP_DELETE)
        {
            sb.delete(offset, offset + length);
        }
        textArea.setText(sb.toString());
    }

}
