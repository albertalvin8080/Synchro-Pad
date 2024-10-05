package org.albert.server.tcp;

import org.albert.util.CompilerProperties;
import org.albert.util.MessageHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GlobalTextHandler
{
    private static final Logger logger = LoggerFactory.getLogger(GlobalTextHandler.class);

    public static final Integer PORT = 7893;
    public static AtomicBoolean available = new AtomicBoolean(true);

    // ============== SINGLETON ==============
    private static final GlobalTextHandler INSTANCE = new GlobalTextHandler();

    public static GlobalTextHandler getInstance()
    {
        return INSTANCE;
    }
    // !============== SINGLETON ==============

    private final ServerSocket serverSocket;
    private final List<ThreadedPadHandlerTcp> globalTextThreads;
    private final List<ThreadedGlobalWritePermission> permissionThreads;

    private GlobalTextHandler()
    {
        try
        {
            this.serverSocket = new ServerSocket(PORT);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        this.permissionThreads = new ArrayList<>();
        this.globalTextThreads = new ArrayList<>();
    }

    public void execute(ThreadedPadHandlerTcp source, MessageHolder msgHolder)
    {
        for (var threadedEchoHandler : globalTextThreads)
        {
            if (threadedEchoHandler == source) continue;

            try
            {
                final ObjectOutputStream newOut = threadedEchoHandler.getOut();
                newOut.writeObject(msgHolder);
                newOut.flush();
                if (CompilerProperties.DEBUG)
                    logger.info("GLOBAL TEXT SENT -> {}", threadedEchoHandler.getUuid());
            }
            catch (SocketException e)
            {
                // Removing threadedEchoHandler if the socket has been closed.
                globalTextThreads.remove(threadedEchoHandler);
                logger.info("Disconnected -> {}", threadedEchoHandler.getUuid());
            }
            catch (Exception e)
            {
                logger.error("{}", e.getStackTrace());
            }
        }
        // Makes the flag available to write
        GlobalTextHandler.available.set(true);
    }

    public void subscribe(ThreadedPadHandlerTcp threadedPadHandlerTcp)
    {
        try
        {
            final Socket accept = serverSocket.accept();
            final ThreadedGlobalWritePermission threadedGlobalWritePermission = new ThreadedGlobalWritePermission(threadedPadHandlerTcp.getUuid(), accept);
            threadedGlobalWritePermission.start();
            this.permissionThreads.add(threadedGlobalWritePermission);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        this.globalTextThreads.add(threadedPadHandlerTcp);
    }

    public void unsubscribe(ThreadedPadHandlerTcp threadedPadHandlerTcp)
    {
        this.globalTextThreads.remove(threadedPadHandlerTcp);
        for (ThreadedGlobalWritePermission permissionThread : permissionThreads)
        {
            if(permissionThread.getUuid().compareTo(threadedPadHandlerTcp.getUuid()) == 0)
            {
                permissionThreads.remove(permissionThread);
                break;
            }
        }
    }

    public void unsubscribe(ThreadedGlobalWritePermission threadedGlobalWritePermission)
    {
        this.permissionThreads.remove(threadedGlobalWritePermission);
        for (ThreadedPadHandlerTcp globalTextThread : globalTextThreads)
        {
            if(globalTextThread.getUuid().compareTo(threadedGlobalWritePermission.getUuid()) == 0)
            {
                globalTextThreads.remove(globalTextThread);
                break;
            }
        }
    }
}
