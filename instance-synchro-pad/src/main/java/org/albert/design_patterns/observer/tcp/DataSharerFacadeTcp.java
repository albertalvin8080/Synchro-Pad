package org.albert.design_patterns.observer.tcp;

import org.albert.CompilerProperties;
import org.albert.component.SynchroPad;
import org.albert.design_patterns.observer.DataSharer;
import org.albert.design_patterns.observer.StateChangeObserver;
import org.albert.util.MessageHolder;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

public class DataSharerFacadeTcp implements StateChangeObserver
{
    private static DataSharerFacadeTcp INSTANCE;

    public static DataSharerFacadeTcp getInstance(SynchroPad synchroPad, JTextArea textArea)
    {
        if (DataSharerFacadeTcp.INSTANCE == null)
            DataSharerFacadeTcp.INSTANCE = new DataSharerFacadeTcp(synchroPad, textArea);
        return DataSharerFacadeTcp.INSTANCE;
    }

    private final SynchroPad synchroPad;
    private final JTextArea textArea;
    private DataSharerTcp dataSharer;
    private UUID uuid;

    private DataSharerFacadeTcp(SynchroPad synchroPad, JTextArea textArea)
    {
        this.synchroPad = synchroPad;
        this.textArea = textArea;
    }

    private Socket socket;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;

    private void initializeNetworking(String serverIp) throws IOException, ClassNotFoundException
    {
        final int port = 1234;
        socket = new Socket(serverIp, port);
        reader = new ObjectInputStream(socket.getInputStream());
        writer = new ObjectOutputStream(socket.getOutputStream());
        MessageHolder initialMessage = (MessageHolder) reader.readObject();
        uuid = UUID.fromString(initialMessage.getUuid());
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
            dataSharer = new DataSharerTcp(uuid, textArea, reader, writer);
            return true;
        }
        // Expected exceptions
        catch (ConnectException | UnknownHostException | NoRouteToHostException e)
        {
            // Connection Refused / UnknownHostException / NoNoRouteToHostException
            if (CompilerProperties.DEBUG)
                e.printStackTrace();
        }
        // Unexpected exceptions
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
        catch (IOException e) // Socket Closed, for example
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
        catch (IOException e) // Socket Closed, for example
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

    // DON'T WORRY ABOUT IT: ~Remember to use nexted if to check for condition on the server side.~
    // ============== SERVER SYNCHRONIZE ==============
    public boolean requestWritePermission()
    {
        return dataSharer.requestWritePermission();
    }
    // !============== SERVER SYNCHRONIZE ==============
}
