package org.albert.server.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.UUID;

public class ThreadedEchoHandler extends Thread
{
    private final UUID uuid;
    private final Socket incoming;
    private final List<ThreadedEchoHandler> threads;
    private final BufferedReader in;
    private final PrintWriter out;

    public ThreadedEchoHandler(Socket i, List<ThreadedEchoHandler> threads) throws IOException
    {
        uuid = UUID.randomUUID();
//        System.out.println(uuid);
        incoming = i;
        this.threads = threads;
        in = new BufferedReader
                (new InputStreamReader(incoming.getInputStream()));
        out = new PrintWriter
                (incoming.getOutputStream(), true /* autoFlush */);
        out.println(uuid); // Sending the UUID
    }

    @Override
    public void run()
    {
        try
        {
            boolean done = false;
            while (!done)
            {
                String msg = in.readLine();
                System.out.println(msg); // debug
                if (msg == null)
                {
                    done = true;
                    continue;
                }

//                out.println("Echo (" + uuid + "): " + msg);

                threads.forEach(threadedEchoHandler -> {
                    if (threadedEchoHandler != this)
                    {
                        try
                        {
                            var socket = threadedEchoHandler.getSocket();
                            PrintWriter newOut = new PrintWriter(socket.getOutputStream(), true);
                            newOut.println(msg);
                        }
                        catch (SocketException e)
                        {
                            // Removing threadedEchoHandler if the socket has been closed.
                            threads.remove(threadedEchoHandler);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

                if (msg.trim().equals("BYE"))
                    done = true;
            }
            incoming.close();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public Socket getSocket()
    {
        return incoming;
    }
}
