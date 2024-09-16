package org.albert.design_patterns.observer.tcp;

import org.albert.design_patterns.observer.DataSharer;
import org.albert.util.MessageHolder;

import javax.swing.*;
import java.io.*;
import java.lang.ref.Cleaner;
import java.net.Socket;
import java.util.UUID;

public class DataSharerTcp implements DataSharer
{
    private final UUID uuid;
    private final JTextArea textArea;
    private final Cleaner.Cleanable cleanable;
    private volatile boolean running;
    private final Thread thread;
    private Socket socket;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;

    public DataSharerTcp(JTextArea textArea) throws IOException, InterruptedException, ClassNotFoundException
    {
        this.textArea = textArea;
        initializeNetworking();

        cleanable = Cleaner.create().register(this, () -> {
            try
            {
                if (socket != null && !socket.isClosed())
                {
                    socket.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });

        running = true;
        MessageHolder initialMessage = (MessageHolder) reader.readObject();
        uuid = UUID.fromString(initialMessage.getUuid());
        System.out.println(uuid);

        thread = createAsyncReceiveThread();
        thread.start();
    }

    private Thread createAsyncReceiveThread()
    {
        return new Thread(() -> {
            while (running)
            {
                try
                {
                    MessageHolder msgHolder = (MessageHolder) reader.readObject();
                    System.out.println("Received: " + msgHolder.getText());

                    short operationType = msgHolder.getOperationType();
                    int offset = msgHolder.getOffset();
                    int length = msgHolder.getLength();
                    String text = msgHolder.getText();

                    final StringBuilder sb = new StringBuilder(textArea.getText());
                    if (operationType == DataSharer.OP_INSERT ||
                            operationType == DataSharer.OP_DELETE)
                    {
                        sb.replace(offset, offset + length, text.equals("null") ? "" : text);
                        textArea.setText(sb.toString());
                    }
                }
                catch (IOException | ClassNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void initializeNetworking() throws IOException
    {
        final int port = 1234;
        socket = new Socket("192.168.1.6", port);
        writer = new ObjectOutputStream(socket.getOutputStream());
        reader = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void destroy()
    {
        running = false;
        thread.interrupt();
        cleanable.clean();
    }

    @Override
    public void onInsert(int offset, int length, String text)
    {
        MessageHolder msgHolder = new MessageHolder(uuid.toString(), OP_INSERT, offset, length, text);
        try
        {
            writer.writeObject(msgHolder);
            writer.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDelete(int offset, int length, String text)
    {
        MessageHolder msgHolder = new MessageHolder(uuid.toString(), OP_DELETE, offset, length, text);
        try
        {
            writer.writeObject(msgHolder);
            writer.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public UUID getUuid()
    {
        return uuid;
    }
}
