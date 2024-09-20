package org.albert.server.tcp;

import org.albert.CompilerProperties;
import org.albert.server.DataSharer;
import org.albert.util.MessageHolder;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.UUID;

public class ThreadedGlobalWritePermission extends Thread
{
    private final Socket socket;
    private final UUID uuid;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final GlobalTextHandler globalTextHandler = GlobalTextHandler.getInstance();

    public ThreadedGlobalWritePermission(UUID uuid, Socket socket) throws IOException
    {
        this.uuid = uuid;
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run()
    {
        try
        {
            boolean done = false;
            while (!done)
            {
                MessageHolder msgHolder = (MessageHolder) in.readObject();
                short operationType = msgHolder.getOperationType();

                if (CompilerProperties.DEBUG)
                    System.out.println("WRITE PERMISSION: " + uuid);

                if (operationType == DataSharer.OP_REQUEST_GLOBAL_WRITE)
                {
                    short response;
                    if (GlobalTextHandler.available.compareAndSet(true, false))
                        response = DataSharer.OP_ACCEPTED_GLOBAL_WRITE;
                    else response = DataSharer.OP_DENIED_GLOBAL_WRITE;

                    if (CompilerProperties.DEBUG)
                        System.out.println("PERMISSION " + (DataSharer.OP_ACCEPTED_GLOBAL_WRITE == response ? "accepted" : "denied") + ": " + uuid);

                    MessageHolder responseMessage = new MessageHolder(null, response, 0, 0, null);

                    out.writeObject(responseMessage);
                    out.flush();
                }
                else
                {
                    if (CompilerProperties.DEBUG)
                        System.out.println("PERMISSION SOCKET Unknown operation type: " + operationType + " -> " + this.uuid);
                }
            }
            socket.close();
        }
        catch (EOFException | SocketException e)
        {
            globalTextHandler.unsubscribe(this);
            if (CompilerProperties.DEBUG)
                System.out.println("PERMISSION SOCKET Disconnected -> " + uuid);
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
}
