package org.albert.server.tcp;

import org.albert.util.MessageHolder;

import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class GlobalTextHandler
{
    // ============== SINGLETON ==============
    private static final GlobalTextHandler INSTANCE = new GlobalTextHandler();

    public static GlobalTextHandler getInstance()
    {
        return INSTANCE;
    }
    // !============== SINGLETON ==============


    private final List<ThreadedPadHandler> globalTextThreads;

    private GlobalTextHandler()
    {
        this.globalTextThreads = new ArrayList<>();
    }

    public void execute(ThreadedPadHandler source, MessageHolder msgHolder)
    {
        for (var threadedEchoHandler : globalTextThreads)
        {
            if (threadedEchoHandler != source)
            {
                try
                {
                    final ObjectOutputStream newOut = threadedEchoHandler.getOut();
                    newOut.writeObject(msgHolder);
                    newOut.flush();
                }
                catch (SocketException e)
                {
                    // Removing threadedEchoHandler if the socket has been closed.
                    globalTextThreads.remove(threadedEchoHandler);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void subscribe(ThreadedPadHandler threadedPadHandler)
    {
        this.globalTextThreads.add(threadedPadHandler);
    }

    public void unsubscribe(ThreadedPadHandler threadedPadHandler)
    {
        this.globalTextThreads.remove(threadedPadHandler);
    }
}
