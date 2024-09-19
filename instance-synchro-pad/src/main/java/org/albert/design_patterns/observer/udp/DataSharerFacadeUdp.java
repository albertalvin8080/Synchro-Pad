package org.albert.design_patterns.observer.udp;

import org.albert.CompilerProperties;
import org.albert.component.SynchroPad;
import org.albert.design_patterns.observer.DataSharer;
import org.albert.design_patterns.observer.StateChangeObserver;
import org.albert.util.MessageHolder;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.UUID;

public class DataSharerFacadeUdp implements StateChangeObserver
{
    private static DataSharerFacadeUdp INSTANCE;

    public static DataSharerFacadeUdp getInstance(SynchroPad synchroPad, JTextArea textArea)
    {
        if (DataSharerFacadeUdp.INSTANCE == null)
            DataSharerFacadeUdp.INSTANCE = new DataSharerFacadeUdp(synchroPad, textArea);
        return DataSharerFacadeUdp.INSTANCE;
    }

    private final SynchroPad synchroPad;
    private final JTextArea textArea;
    private DataSharerUdp dataSharer;
    private UUID uuid;

    private DataSharerFacadeUdp(SynchroPad synchroPad, JTextArea textArea)
    {
        this.synchroPad = synchroPad;
        this.textArea = textArea;
    }

    private DatagramSocket socket;
    private InetAddress serverAddress;

    private void initializeNetworking(String serverIp) throws IOException, ClassNotFoundException
    {
        final int port = 1234;
        socket = new DatagramSocket(); // UDP uses DatagramSocket
        serverAddress = InetAddress.getByName(serverIp);
        
        // Send initial message to the server to register
        MessageHolder initialMessage = new MessageHolder(null, DataSharer.OP_INIT_GLOBAL, 0, 0, "");
        sendMessage(initialMessage);
        
        // Receive UUID from server
        MessageHolder responseMessage = receiveMessage();
        uuid = UUID.fromString(responseMessage.getUuid());
    }

    public UUID getUuid()
    {
        return this.uuid;
    }

    public boolean openConnection(String serverIp)
    {
        if (this.dataSharer != null)
            return false;
        try
        {
            initializeNetworking(serverIp);
            dataSharer = new DataSharerUdp(uuid, textArea, socket, serverAddress);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public void closeConnection()
    {
        if (this.dataSharer == null)
            return;
        dataSharer.destroy();
        dataSharer = null;
    }

    @Override
    public void onInsert(int offset, int length, String text)
    {
        if (dataSharer == null) return;
        try
        {
            dataSharer.onInsert(offset, length, text);
        }
        catch (IOException e)
        {
            handleSocketClosed();
        }
    }

    @Override
    public void onDelete(int offset, int length, String text)
    {
        if (dataSharer == null) return;
        try
        {
            dataSharer.onDelete(offset, length, text);
        }
        catch (IOException e)
        {
            handleSocketClosed();
        }
    }

    private void handleSocketClosed()
    {
        System.out.println("SOCKET CLOSED FORCEFULLY");
        JOptionPane.showMessageDialog(
                synchroPad, "Disconnected from server", "Error", JOptionPane.ERROR_MESSAGE
        );
        synchroPad.disconnect();
    }

    // Send UDP message
    private void sendMessage(MessageHolder message) throws IOException
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteStream);
        out.writeObject(message);
        out.flush();
        byte[] sendData = byteStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, 1234);
        socket.send(sendPacket);
    }

    // Receive UDP message
    private MessageHolder receiveMessage() throws IOException, ClassNotFoundException
    {
        byte[] receiveData = new byte[1024];  // You may need to adjust the buffer size
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        ByteArrayInputStream byteStream = new ByteArrayInputStream(receivePacket.getData());
        ObjectInputStream in = new ObjectInputStream(byteStream);
        return (MessageHolder) in.readObject();
    }
}
