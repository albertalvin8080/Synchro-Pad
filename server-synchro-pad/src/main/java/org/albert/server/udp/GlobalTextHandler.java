package org.albert.server.udp;

import org.albert.util.CompilerProperties;
import org.albert.util.MessageHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GlobalTextHandler
{
    private static final Logger logger = LoggerFactory.getLogger(GlobalTextHandler.class);

    public static AtomicBoolean available = new AtomicBoolean(true);

    // ============== SINGLETON ==============
    private static final GlobalTextHandler INSTANCE = new GlobalTextHandler();

    public static GlobalTextHandler getInstance()
    {
        return INSTANCE;
    }
    // !============== SINGLETON ==============


    private final List<ThreadedPadHandlerUdp> globalTextThreads;

    private GlobalTextHandler()
    {
        this.globalTextThreads = new ArrayList<>();
    }

    public void execute(ThreadedPadHandlerUdp source, MessageHolder msgHolder)
    {
        for (var threadedPadHandler : globalTextThreads)
        {
            if (threadedPadHandler == source) continue;

            try
            {
                byte[] data = msgHolder.serialize();

                DatagramPacket packet = new DatagramPacket(data, data.length, threadedPadHandler.getAddress(), threadedPadHandler.getPort());

                DatagramSocket socket = threadedPadHandler.getSocket();
                socket.send(packet);

                if (CompilerProperties.DEBUG) logger.info("GLOBAL TEXT SENT -> " + threadedPadHandler.getUuid());
            }
            catch (SocketException e)
            {
                // Removing threadedPadHandler if the socket has been closed or is unreachable.
                globalTextThreads.remove(threadedPadHandler);
            }
            catch (Exception e)
            {
                logger.error("{}", e.getStackTrace());
            }
        }

        // Makes the flag available to write
        GlobalTextHandler.available.set(true);
    }

    public void subscribe(ThreadedPadHandlerUdp threadedPadHandlerUdp)
    {
        this.globalTextThreads.add(threadedPadHandlerUdp);
    }

    public void unsubscribe(ThreadedPadHandlerUdp threadedPadHandlerUdp)
    {
        this.globalTextThreads.remove(threadedPadHandlerUdp);
    }
}
