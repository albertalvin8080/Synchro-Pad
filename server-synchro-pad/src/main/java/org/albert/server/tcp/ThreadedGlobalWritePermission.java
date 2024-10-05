package org.albert.server.tcp;

import org.albert.design_patterns.observer.DataSharer;
import org.albert.util.CompilerProperties;
import org.albert.util.MessageHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

public class ThreadedGlobalWritePermission extends Thread
{
    private static final Logger logger = LoggerFactory.getLogger(ThreadedGlobalWritePermission.class);

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
                    logger.info("WRITE PERMISSION: " + uuid);

                if (operationType == DataSharer.OP_REQUEST_GLOBAL_WRITE)
                {
                    short response;
                    if (GlobalTextHandler.available.compareAndSet(true, false))
                        response = DataSharer.OP_ACCEPTED_GLOBAL_WRITE;
                    else response = DataSharer.OP_DENIED_GLOBAL_WRITE;

                    if (CompilerProperties.DEBUG)
                        logger.info("PERMISSION " + (DataSharer.OP_ACCEPTED_GLOBAL_WRITE == response ? "accepted" : "denied") + ": " + uuid);

                    MessageHolder responseMessage = new MessageHolder(null, response, 0, 0, null);

                    out.writeObject(responseMessage);
                    out.flush();
                }
                else
                {
                    if (CompilerProperties.DEBUG)
                        logger.info("PERMISSION SOCKET Unknown operation type: " + operationType + " -> " + this.uuid);
                }
            }
            socket.close();
        }
        catch (EOFException | SocketException e)
        {
            globalTextHandler.unsubscribe(this);
            if (CompilerProperties.DEBUG)
                logger.info("PERMISSION SOCKET Disconnected -> " + uuid);
        }
        catch (Exception e)
        {
            logger.error("{}", e.getStackTrace());
        }
    }

    public UUID getUuid()
    {
        return uuid;
    }
}
