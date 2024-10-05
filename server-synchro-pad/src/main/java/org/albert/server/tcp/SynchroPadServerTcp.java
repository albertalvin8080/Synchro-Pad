package org.albert.server.tcp;

import org.albert.util.CompilerProperties;
import org.albert.util.SharedFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SynchroPadServerTcp
{
    private static final Logger logger = LoggerFactory.getLogger(SynchroPadServerTcp.class);

    private StringBuilder sb;
    private boolean running = true;

    public void init()
    {
        sb = SharedFileUtils.readFromSharedFile();
        final Thread writeToSharedFileThread = createWriteToSharedFileThread();
        writeToSharedFileThread.start();

        logger.info("Commence tcp server...");
        int i = 1;
        try (ServerSocket s = new ServerSocket(1234))
        {
            List<ThreadedPadHandlerTcp> allThreads = new ArrayList<>();
            while (running)
            {
                Socket incoming = s.accept();
                if (CompilerProperties.DEBUG)
                    logger.info("Spawning {}", i);
                var thread = new ThreadedPadHandlerTcp(incoming, allThreads, sb);
                thread.start();
                allThreads.add(thread);
                i++;
            }
        }
        catch (Exception e)
        {
            logger.error("{}", e.getStackTrace());
        }
    }

    private Thread createWriteToSharedFileThread()
    {
        return new Thread(() -> {
            while (running)
            {
                try
                {
//                    Thread.sleep(Duration.ofMinutes(1).toMillis());
                    Thread.sleep(Duration.ofSeconds(3).toMillis()); // Debug
                    SharedFileUtils.writeToSharedFile(sb);
//                    logger.info("Written"); // Debug
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}

