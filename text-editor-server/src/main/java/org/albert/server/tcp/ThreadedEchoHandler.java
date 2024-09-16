package org.albert.server.tcp;

import org.albert.util.MessageHolder;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.UUID;

public class ThreadedEchoHandler extends Thread
{
    private final UUID uuid;
    private final Socket incoming;
    private final List<ThreadedEchoHandler> threads;
    /*
    * Fun fact: you must not create a new ObjectOutputStream nor ObjectInputStream
    * because it corrupts the header.
    * >>
    * src: https://stackoverflow.com/questions/2393179/streamcorruptedexception-invalid-type-code-ac
    * */
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public ThreadedEchoHandler(Socket i, List<ThreadedEchoHandler> threads) throws IOException
    {
        uuid = UUID.randomUUID();
        incoming = i;
        this.threads = threads;

        // Object streams to handle MessageHolder objects
        out = new ObjectOutputStream(incoming.getOutputStream());
        in = new ObjectInputStream(incoming.getInputStream());

        // Sending the UUID in an initial message
        MessageHolder initialMessage = new MessageHolder(uuid.toString(), (short) 0, 0, 0, "Connected");
        out.writeObject(initialMessage);
        out.flush();
        System.out.println("Sent -> " + initialMessage.getUuid());
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

                if (msgHolder == null)
                {
                    done = true;
                    continue;
                }

                System.out.println("Received: " + msgHolder.getText()); // Debugging

                threads.forEach(threadedEchoHandler -> {
                    if (threadedEchoHandler != this)
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
                            threads.remove(threadedEchoHandler);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

                // Check for termination signal
                if (msgHolder.getText().trim().equals("BYE"))
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

    public UUID getUuid()
    {
        return uuid;
    }

    public Socket getIncoming()
    {
        return incoming;
    }

    public List<ThreadedEchoHandler> getThreads()
    {
        return threads;
    }

    public ObjectInputStream getIn()
    {
        return in;
    }

    public ObjectOutputStream getOut()
    {
        return out;
    }
}
