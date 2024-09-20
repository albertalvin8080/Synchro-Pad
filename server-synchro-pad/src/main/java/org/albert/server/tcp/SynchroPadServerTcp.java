package org.albert.server.tcp;

import org.albert.CompilerProperties;
import org.albert.util.SharedFileUtils;

import java.net.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SynchroPadServerTcp
{
    private StringBuilder sb;
    private boolean running = true;

    public void init()
    {
        sb = SharedFileUtils.readFromSharedFile();
        final Thread writeToSharedFileThread = createWriteToSharedFileThread();
        writeToSharedFileThread.start();

        System.out.println("Commence tcp server...");
        int i = 1;
        try (ServerSocket s = new ServerSocket(1234))
        {
            List<ThreadedPadHandlerTcp> allThreads = new ArrayList<>();
            while (running)
            {
                Socket incoming = s.accept();
                if (CompilerProperties.DEBUG)
                    System.out.println("Spawning " + i);
                var thread = new ThreadedPadHandlerTcp(incoming, allThreads, sb);
                thread.start();
                allThreads.add(thread);
                i++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
//                    System.out.println("Written"); // Debug
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}

