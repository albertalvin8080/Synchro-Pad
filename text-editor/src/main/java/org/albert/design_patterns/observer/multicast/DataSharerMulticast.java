package org.albert.design_patterns.observer.multicast;

import org.albert.design_patterns.observer.DataSharer;

import javax.swing.*;
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.net.*;
import java.util.UUID;

public class DataSharerMulticast implements DataSharer
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
    private final MessageHandler messageHandler;

    public DataSharerMulticast(JTextArea textArea) throws IOException, InterruptedException
    {
        this.textArea = textArea;
        this.uuid = UUID.randomUUID();
        initializeNetworking();
        this.messageHandler = new MessageHandler(multicastSocket, mcastaddr, uuid, textArea);

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
            messageHandler.sendMessage(OP_NEW_REQUEST, 0, 0, "");
            boolean initConfimed = false;

            StringBuilder sb = new StringBuilder("");
            while (running)
            {
                DatagramPacket dIn = messageHandler.receiveMessage();
                if (dIn == null)
                {
                    System.out.println(initConfimed);
                    if (!initConfimed)
                        messageHandler.sendMessage(OP_NEW_REQUEST, 0, 0, "");
                    continue;
                }

                String[] parts = messageHandler.extractMessage(dIn);
                String senderId = parts[0];
                short operationType = Short.parseShort(parts[1]);
                String text = parts[4];

                if (!senderId.equals(this.uuid.toString())) continue;

                if (operationType == DataSharerMulticast.OP_NEW_RESPONSE)
                {
                    sb.append(text);
                    var packet = messageHandler.constructPackage(senderId, OP_NEW_REQUEST_RECEIVED, 0, 0, "");
                    multicastSocket.send(packet);
                }
                else if (operationType == DataSharerMulticast.OP_NEW_RESPONSE_END)
                {
                    sb.append(text);
                    break;
                }
                else if (operationType == DataSharerMulticast.OP_NEW_REQUEST_INIT)
                    initConfimed = true;
            }
            textArea.setText(sb.toString());
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

    private Thread createAsyncReceiveThread()
    {
        return new Thread(() -> {
            System.out.println("Thread listening...");
            while (running)
            {
                DatagramPacket dIn = messageHandler.receiveMessage();
                if (dIn == null) continue;

                String[] parts = messageHandler.extractMessage(dIn);
                String senderId = parts[0];
                short operationType = Short.parseShort(parts[1]);
                int offset = Integer.parseInt(parts[2]);
                int length = Integer.parseInt(parts[3]);
                String text = parts[4];

                // Prevents intercepting its own messages.
                if (!senderId.equals(uuid.toString()))
                {
                    messageHandler.processMessage(operationType, offset, length, text);
                }
            }
        });
    }

    public void destroy()
    {
        running = false;
        thread.interrupt();
        cleanable.clean();
    }

    private void share(int offset, int length, String text, short operationType)
    {
        messageHandler.sendMessage(operationType, offset, length, text);
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
