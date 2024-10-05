package org.albert.server.tcp;

import org.albert.design_patterns.observer.DataSharer;
import org.albert.util.CompilerProperties;
import org.albert.util.MessageHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.UUID;

public class ThreadedPadHandlerTcp extends Thread
{
    private static final Logger logger = LoggerFactory.getLogger(ThreadedPadHandlerTcp.class);

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
        logger.info("Init -> " + initialMessage.getUuid());
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
                    logger.info("Received: " + text.replaceAll("\n", " \\\\{nl} "));

                switch (operationType)
                {
                    case DataSharer.OP_DISCONNECT_GLOBAL:
                        allThreads.remove(this);
                        globalTextHandler.unsubscribe(this);
                        logger.info("Disconnected -> " + uuid);
                        break;

                    case DataSharer.OP_INSERT:
                    case DataSharer.OP_DELETE:
                        if (CompilerProperties.DEBUG)
                            logger.info("INSERT | DELETE -> " + this.uuid);
                        sb.replace(offset, offset + length, text);
                        globalTextHandler.execute(this, msgHolder);
                        break;

                    case DataSharer.OP_INIT_GLOBAL:
                        if (CompilerProperties.DEBUG)
                            logger.info("SUBSCRIBE -> " + this.uuid);
                        MessageHolder initialMessage = new MessageHolder(
                                null, (short) 0, 0, 0, sb.toString()
                        );
                        initialMessage.setInfo(String.valueOf(GlobalTextHandler.PORT));
                        out.writeObject(initialMessage);
                        out.flush();
                        globalTextHandler.subscribe(this);
                        break;

                    default:
                        if (CompilerProperties.DEBUG)
                            logger.info("Unknown operation type: " + operationType + " -> " + this.uuid);
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
            logger.info("Disconnected -> " + uuid);
        }
        catch (Exception e)
        {
            logger.error("{}", e.getStackTrace());
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
