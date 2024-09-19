package org.albert.server.tcp;

import org.albert.CompilerProperties;
import org.albert.util.MessageHolder;

import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GlobalTextHandler
{
    public static AtomicBoolean available = new AtomicBoolean(true);

    // ============== SINGLETON ==============
    private static final GlobalTextHandler INSTANCE = new GlobalTextHandler();

    public static GlobalTextHandler getInstance()
    {
        return INSTANCE;
    }
    // !============== SINGLETON ==============


    private final List<ThreadedPadHandlerTcp> globalTextThreads;

    private GlobalTextHandler()
    {
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
                    System.out.println("GLOBAL TEXT SENT -> " + threadedEchoHandler.getUuid());
            }
            catch (SocketException e)
            {
                // Removing threadedEchoHandler if the socket has been closed.
                globalTextThreads.remove(threadedEchoHandler);
                System.out.println("Disconnected -> " + threadedEchoHandler.getUuid());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        // Makes the flag available to write
        GlobalTextHandler.available.set(true);
    }

    public void subscribe(ThreadedPadHandlerTcp threadedPadHandlerTcp)
    {
        this.globalTextThreads.add(threadedPadHandlerTcp);
    }

    public void unsubscribe(ThreadedPadHandlerTcp threadedPadHandlerTcp)
    {
        this.globalTextThreads.remove(threadedPadHandlerTcp);
    }
}
