package org.albert.server.tcp;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class TextEditorServerTcp
{
    public void init()
    {
        System.out.println("Commence tcp server...");
        int i = 1;
        try (ServerSocket s = new ServerSocket(1234))
        {
            List<ThreadedEchoHandler> threads = new ArrayList<>();
            for (; ; )
            {
                Socket incoming = s.accept();
                System.out.println("Spawning " + i);
                var thread = new ThreadedEchoHandler(incoming, threads);
                thread.start();
                threads.add(thread);
                i++;
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}

