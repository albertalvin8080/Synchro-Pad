package org.albert.server.tcp;

import org.albert.CompilerProperties;
import org.albert.server.DataSharer;
import org.albert.util.MessageHolder;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.UUID;

public class ThreadedPadHandler extends Thread
{
    private final UUID uuid;
    private final Socket incoming;
    private final List<ThreadedPadHandler> allThreads;
    private final GlobalTextHandler globalTextHandler;
    /*
     * Fun fact: you must not create a new ObjectOutputStream nor ObjectInputStream
     * because it corrupts the header. Just use these two below.
     * ->> src: https://stackoverflow.com/questions/2393179/streamcorruptedexception-invalid-type-code-ac
     * */
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final StringBuilder sb;

    public ThreadedPadHandler(Socket i, List<ThreadedPadHandler> allThreads, StringBuilder sb) throws IOException
    {
        uuid = UUID.randomUUID();
        incoming = i;
        this.allThreads = allThreads;
        this.globalTextHandler = GlobalTextHandler.getInstance();

        // Object streams to handle MessageHolder objects
        out = new ObjectOutputStream(incoming.getOutputStream());
        in = new ObjectInputStream(incoming.getInputStream());
        this.sb = sb;

        // Sending the UUID in an initial message
        MessageHolder initialMessage = new MessageHolder(uuid.toString(), (short) 0, 0, 0, "");
        out.writeObject(initialMessage);
        out.flush();
//        if (CompilerProperties.DEBUG)
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
                text = text == null ? "" : text;

                if (CompilerProperties.DEBUG)
                    System.out.println("Received: " + text.replaceAll("\n", " \\\\{nl} "));

                if (operationType == DataSharer.OP_INSERT ||
                        operationType == DataSharer.OP_DELETE)
                {
                    if (CompilerProperties.DEBUG)
                        System.out.println("INSERT | DELETE -> " + this.uuid);
                    sb.replace(offset, offset + length, text);
                    globalTextHandler.execute(this, msgHolder);
                }
                else if (operationType == DataSharer.OP_INIT_GLOBAL)
                {
                    if (CompilerProperties.DEBUG)
                        System.out.println("SUBSCRIBE -> " + this.uuid);
                    globalTextHandler.subscribe(this);
                    MessageHolder initialMessage = new MessageHolder("", (short) 0, 0, 0, sb.toString());
                    out.writeObject(initialMessage);
                    out.flush();
                }
            }
            incoming.close();
        }
        catch (EOFException | SocketException e)
        {
            globalTextHandler.unsubscribe(this);
            allThreads.remove(this);
//            if (CompilerProperties.DEBUG)
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

    public UUID getUuid()
    {
        return uuid;
    }
}
