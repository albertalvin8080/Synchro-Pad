package org.albert.design_patterns.observer.tcp;

import org.albert.design_patterns.observer.DataSharer;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ref.Cleaner;
import java.net.Socket;
import java.util.UUID;

public class DataSharerTcp implements DataSharer
{
    public static final short OP_INSERT = 1;
    public static final short OP_DELETE = 2;

    private final UUID uuid;
    private final JTextArea textArea;
    private final Cleaner.Cleanable cleanable;
    private volatile boolean running;
//    private final Thread thread;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public DataSharerTcp(JTextArea textArea) throws IOException, InterruptedException
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
        uuid = UUID.fromString(reader.readLine());
        System.out.println(uuid);

//        thread = createAsyncReceiveThread();
//        thread.start();
    }

    private void initializeNetworking() throws IOException
    {
        final int port = 1234;
        socket = new Socket("192.168.1.6", port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void destroy()
    {

    }

    @Override
    public void onInsert(int offset, int length, String text)
    {
        String msg = new StringBuilder(uuid.toString())
                .append(":").append(OP_INSERT)
                .append(":").append(offset)
                .append(":").append(length)
                .append(":").append(text)
                .toString();
        writer.print(msg);
    }

    @Override
    public void onDelete(int offset, int length, String text)
    {
        String msg = new StringBuilder(uuid.toString())
                .append(":").append(OP_DELETE)
                .append(":").append(offset)
                .append(":").append(length)
                .append(":").append(text)
                .toString();
        writer.print(msg);
    }

    public UUID getUuid()
    {
        return uuid;
    }
}
