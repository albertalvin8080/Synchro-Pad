package org.albert.server.udp;

import org.albert.server.DataSharer;
import org.albert.util.MessageHolder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

public class ThreadedPadHandlerUdp extends Thread
{
    private final UUID uuid;
    private final DatagramSocket socket;
    private final InetAddress address;
    private final int port;
    private final List<ThreadedPadHandlerUdp> allHandlers;
    private final GlobalTextHandler globalTextHandler;
    private final StringBuilder sb;

    public ThreadedPadHandlerUdp(DatagramSocket socket, InetAddress address, int port, List<ThreadedPadHandlerUdp> allHandlers, StringBuilder sb)
    {
        this.uuid = UUID.randomUUID();
        this.socket = socket;
        this.address = address;
        this.port = port;
        this.allHandlers = allHandlers;
        this.globalTextHandler = GlobalTextHandler.getInstance();
        this.sb = sb;

        sendMessage(new MessageHolder(uuid.toString(), (short) 0, 0, 0, null));
        System.out.println("Init -> " + uuid);
    }

    @Override
    public void run()
    {
        try
        {
            boolean done = false;
            byte[] buffer = new byte[1024]; // Adjust buffer size if needed
            while (!done)
            {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                MessageHolder msgHolder = MessageHolder.deserialize(packet.getData());

                // Handle operations similarly
                handleMessage(msgHolder);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void handleMessage(MessageHolder msgHolder)
    {
        short operationType = msgHolder.getOperationType();
        String text = msgHolder.getText() == null ? "" : msgHolder.getText();

        switch (operationType)
        {
            case DataSharer.OP_INSERT:
            case DataSharer.OP_DELETE:
                sb.replace(msgHolder.getOffset(), msgHolder.getOffset() + msgHolder.getLength(), text);
                globalTextHandler.execute(this, msgHolder);
                break;
            case DataSharer.OP_INIT_GLOBAL:
                globalTextHandler.subscribe(this);
                sendMessage(new MessageHolder(null, (short) 0, 0, 0, sb.toString()));
                break;
            default:
                System.out.println("Unknown operation type: " + operationType);
                break;
        }
    }

    private void sendMessage(MessageHolder message)
    {
        try
        {
            byte[] data = message.serialize();
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public DatagramSocket getSocket()
    {
        return socket;
    }

    public InetAddress getAddress()
    {
        return address;
    }

    public int getPort()
    {
        return port;
    }

    public List<ThreadedPadHandlerUdp> getAllHandlers()
    {
        return allHandlers;
    }

    public GlobalTextHandler getGlobalTextHandler()
    {
        return globalTextHandler;
    }

    public StringBuilder getSb()
    {
        return sb;
    }
}
