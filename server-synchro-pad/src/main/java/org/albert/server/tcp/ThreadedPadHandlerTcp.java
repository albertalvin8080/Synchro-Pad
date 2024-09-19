package org.albert.server.tcp;

import org.albert.CompilerProperties;
import org.albert.server.DataSharer;
import org.albert.util.MessageHolder;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.UUID;

public class ThreadedPadHandlerTcp extends Thread
{
    private final UUID uuid;
    private final Socket incoming;
    private final List<ThreadedPadHandlerTcp> allThreads;
    private final GlobalTextHandler globalTextHandler;
    /*
     * Fun fact: you must not create a new ObjectOutputStream nor ObjectInputStream
     * because it corrupts the header. Just use these two below.
     * ->> src: https://stackoverflow.com/questions/2393179/streamcorruptedexception-invalid-type-code-ac
     * */
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final StringBuilder sb;

    public ThreadedPadHandlerTcp(Socket i, List<ThreadedPadHandlerTcp> allThreads, StringBuilder sb) throws IOException
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
        MessageHolder initialMessage = new MessageHolder(uuid.toString(), (short) 0, 0, 0, null);
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

                switch (operationType)
                {
                    case DataSharer.OP_DISCONNECT_GLOBAL:
                        allThreads.remove(this);
                        globalTextHandler.unsubscribe(this);
                        System.out.println("Disconnected -> " + uuid);
                        break;

                    case DataSharer.OP_INSERT:
                    case DataSharer.OP_DELETE:
                        if (CompilerProperties.DEBUG)
                            System.out.println("INSERT | DELETE -> " + this.uuid);
                        sb.replace(offset, offset + length, text);
                        globalTextHandler.execute(this, msgHolder);
                        break;

                    case DataSharer.OP_INIT_GLOBAL:
                        if (CompilerProperties.DEBUG)
                            System.out.println("SUBSCRIBE -> " + this.uuid);
                        globalTextHandler.subscribe(this);
                        MessageHolder initialMessage = new MessageHolder(
                                null, (short) 0, 0, 0, sb.toString()
                        );
                        out.writeObject(initialMessage);
                        out.flush();
                        break;

                    case DataSharer.OP_REQUEST_GLOBAL_WRITE:
                        short response;
                        if (GlobalTextHandler.available.compareAndSet(true, false))
                            response = DataSharer.OP_ACCEPTED_GLOBAL_WRITE;
                        else
                            response = DataSharer.OP_DENIED_GLOBAL_WRITE;

                        while (true)
                        {
                            if (CompilerProperties.DEBUG)
                                System.out.println("SENDING RESPONSE...");

                            MessageHolder responseMessage = new MessageHolder(
                                    null, response, 0, 0, null
                            );

                            Thread.sleep(100);
                            out.writeObject(responseMessage);
                            out.flush();

                            MessageHolder confirmation = (MessageHolder) in.readObject();
                            if (confirmation.getOperationType() == DataSharer.OP_CLIENT_CONFIRMATION_GLOBAL_WRITE)
                                break;
                        }

                        if (CompilerProperties.DEBUG)
                        {
                            System.out.println("REQUEST GLOBAL WRITE (after loop) -> " + this.uuid);
                            System.out.println(response == DataSharer.OP_ACCEPTED_GLOBAL_WRITE ? "Accepted" : "Denied");
                        }
                        break;

                    default:
                        if (CompilerProperties.DEBUG)
                            System.out.println("Unknown operation type: " + operationType + " -> " + this.uuid);
                        break;
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
