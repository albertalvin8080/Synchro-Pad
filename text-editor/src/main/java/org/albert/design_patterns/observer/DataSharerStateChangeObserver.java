package org.albert.design_patterns.observer;

import javax.swing.*;
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.net.*;
import java.util.Arrays;
import java.util.UUID;

public class DataSharerStateChangeObserver implements StateChangeObserver
{
    public static final short OP_BREAK = -1;
    public static final short OP_INSERT = 1;
    public static final short OP_DELETE = 2;
    public static final short OP_NEW_REQUEST = 10;
    public static final short OP_NEW_RESPONSE = 11;
    public static final short OP_NEW_RESPONSE_END = 12;
    public static final short OP_NEW_REQUEST_INIT = 13;
    public static final short OP_NEW_REQUEST_RECEIVED = 14;

    private final UUID uuid;
    private InetAddress mcastaddr;
    private InetSocketAddress group;
    private NetworkInterface netIf;
    private MulticastSocket multicastSocket;

    private final JTextArea textArea;
    private final Cleaner.Cleanable cleanable;
    private volatile boolean running;
    private final Thread thread;

    public DataSharerStateChangeObserver(JTextArea textArea) throws IOException, InterruptedException
    {
        this.textArea = textArea;
        this.uuid = UUID.randomUUID();
        initializeNetworking();
        cleanable = Cleaner.create().register(this, () -> {
            try
            {
                multicastSocket.leaveGroup(group, netIf);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (multicastSocket != null)
                {
                    multicastSocket.close();
                }
            }
        });

        running = true;
        System.out.println("Waiting for init...");
        init();
        System.out.println("Init finished.");

        thread = createAsyncReceiveThread();
        thread.start();
    }

    private void initializeNetworking() throws IOException
    {
        mcastaddr = InetAddress.getByName("229.1.1.1");
        netIf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        final int port = 1234;
        group = new InetSocketAddress(mcastaddr, port);
        multicastSocket = new MulticastSocket(port);
        multicastSocket.joinGroup(group, netIf);
    }

    private void init()
    {
        try
        {
            multicastSocket.setSoTimeout(5000); // Set timeout to 5 seconds
            sendMessage(OP_NEW_REQUEST, 0, 0, "");
            boolean initConfimed = false;

            textArea.setText("");
            while (running)
            {
                DatagramPacket dIn = receiveMessage();
                if (dIn == null)
                {
                    System.out.println(initConfimed);
                    if (initConfimed)
                    {
                        continue;
                    }
                    sendMessage(OP_NEW_REQUEST, 0, 0, "");
                    continue;
                }

//                System.out.println("BODY");

                String[] parts = extractMessage(dIn);
                String senderId = parts[0];
                short operationType = Short.parseShort(parts[1]);
                String text = parts[4];

                if (!senderId.equals(this.uuid.toString())) continue;

                if (operationType == DataSharerStateChangeObserver.OP_NEW_RESPONSE)
                {
                    textArea.setText(textArea.getText() + text);
                    var packet = constructPackage(senderId, OP_NEW_REQUEST_RECEIVED, 0, 0, "");
                    multicastSocket.send(packet);
                }
                else if (operationType == DataSharerStateChangeObserver.OP_NEW_RESPONSE_END)
                {
                    textArea.setText(textArea.getText() + text);
                    break;
                }
                else if (operationType == DataSharerStateChangeObserver.OP_NEW_REQUEST_INIT)
                    initConfimed = true;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                multicastSocket.setSoTimeout(0);  // Set timeout back to infinite (no timeout)
                System.out.println("Timeout reset to infinite.");
            }
            catch (SocketException e)
            {
                e.printStackTrace();
            }
        }
    }

    private DatagramPacket constructPackage(String senderId, short operationType, int offset, int length, String text)
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

    private Thread createAsyncReceiveThread()
    {
        return new Thread(() -> {
            System.out.println("Thread listening...");
            while (running)
            {
                DatagramPacket dIn = receiveMessage();
                if (dIn == null) continue;

                String[] parts = extractMessage(dIn);
                String senderId = parts[0];
                short operationType = Short.parseShort(parts[1]);
                int offset = Integer.parseInt(parts[2]);
                int length = Integer.parseInt(parts[3]);
                String text = parts[4];

                // Prevents intercepting its own messages.
                if (!senderId.equals(uuid.toString()))
                {
                    processMessage(operationType, offset, length, text);
                }
            }
        });
    }

    private void sendMessage(short operationType, int offset, int length, String text)
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

    private DatagramPacket receiveMessage()
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

    private String[] extractMessage(DatagramPacket packet)
    {
        String str = new String(packet.getData(), 0, packet.getLength());
        final String[] split = str.split(":", 5);
        // Testing text == null scenario.
//        System.out.println(split[4]);
        return split;
    }

    private void processMessage(short operationType, int offset, int length, String text)
    {
        System.out.println("DATA SHARER");
        System.out.println(Arrays.toString(
                new Object[]{offset, length, text, operationType}
        ));

        final StringBuilder sb = new StringBuilder(textArea.getText());
        if (operationType == DataSharerStateChangeObserver.OP_INSERT ||
                operationType == DataSharerStateChangeObserver.OP_DELETE)
        {
            sb.replace(offset, offset + length, text.equals("null") ? "" : text);
            textArea.setText(sb.toString());
        }
    }

    public void destroy()
    {
        running = false;
        thread.interrupt();
        cleanable.clean();
    }

    private void share(int offset, int length, String text, short operationType)
    {
        sendMessage(operationType, offset, length, text);
    }

    @Override
    public void onInsert(int offset, int length, String text)
    {
        share(offset, length, text, OP_INSERT);
    }

    @Override
    public void onDelete(int offset, int length, String text)
    {
        share(offset, length, text, OP_DELETE);
    }

    public UUID getUuid()
    {
        return uuid;
    }
}
