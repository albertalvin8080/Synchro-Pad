package org.albert.server.tcp;

import org.albert.server.DataSharer;
import org.albert.util.MessageHolder;
import org.albert.util.SharedFileUtils;

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
     * because it corrupts the header. Just use these two below.
     * ->> src: https://stackoverflow.com/questions/2393179/streamcorruptedexception-invalid-type-code-ac
     * */
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final StringBuilder sb;

    public ThreadedEchoHandler(Socket i, List<ThreadedEchoHandler> threads, StringBuilder sb) throws IOException
    {
        uuid = UUID.randomUUID();
        incoming = i;
        this.threads = threads;

        // Object streams to handle MessageHolder objects
        out = new ObjectOutputStream(incoming.getOutputStream());
        in = new ObjectInputStream(incoming.getInputStream());
        this.sb = sb;

        // Sending the UUID in an initial message
        MessageHolder initialMessage = new MessageHolder(uuid.toString(), (short) 0, 0, 0, "");
        out.writeObject(initialMessage);
        out.flush();
        System.out.println("Init -> " + initialMessage.getUuid());
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
                int offset = msgHolder.getOffset();
                int length = msgHolder.getLength();
                String text = msgHolder.getText();

                // those two should be placed into its own class
                if (operationType == DataSharer.OP_INSERT ||
                        operationType == DataSharer.OP_DELETE)
                {
                    sb.replace(offset, offset + length, text == null ? "" : text);
                }
                else if(operationType == DataSharer.OP_INIT_GLOBAL)
                {
                    MessageHolder initialMessage = new MessageHolder("", (short) 0, 0, 0, sb.toString());
                    out.writeObject(initialMessage);
                    out.flush();
                    // here would happen the subscription to the global text thread list.
                    continue;
                }

                System.out.println("Received: " + msgHolder.getText()); // Debugging

                for (var threadedEchoHandler : threads)
                {
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
                }
            }
            incoming.close();
        }
        catch (EOFException | SocketException e)
        {
            threads.remove(this);
            System.out.println("Disconnected -> " + uuid);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public ObjectOutputStream getOut()
    {
        return out;
    }
}
