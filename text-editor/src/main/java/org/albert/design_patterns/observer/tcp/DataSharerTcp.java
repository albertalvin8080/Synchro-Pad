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
    private final UUID uuid;
    private final JTextArea textArea;
    private final Cleaner.Cleanable cleanable;
    private volatile boolean running;
    private final Thread thread;
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
                    String msg = reader.readLine();
                    System.out.println("[" + msg + "]");

                    String[] parts = msg.split(":", 5);
//                    String senderId = parts[0];
                    short operationType = Short.parseShort(parts[1]);
                    int offset = Integer.parseInt(parts[2]);
                    int length = Integer.parseInt(parts[3]);
                    String text = parts[4];
                    System.out.println(text);
                    text = text.replaceAll("\\$n", "\n");
                    System.out.println(text);

                    final StringBuilder sb = new StringBuilder(textArea.getText());
                    if (operationType == DataSharer.OP_INSERT ||
                            operationType == DataSharer.OP_DELETE)
                    {
                        sb.replace(offset, offset + length, text.equals("null") ? "" : text);
                        textArea.setText(sb.toString());
                    }
                }
                catch (IOException e)
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
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
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
        String msg = new StringBuilder(uuid.toString())
                .append(":").append(OP_INSERT)
                .append(":").append(offset)
                .append(":").append(length)
                .append(":").append(text.replaceAll("\n", "$n"))
                .toString();
//        System.out.println("["+msg+"]");
        writer.println(msg);
    }

    @Override
    public void onDelete(int offset, int length, String text)
    {
        String msg = new StringBuilder(uuid.toString())
                .append(":").append(OP_DELETE)
                .append(":").append(offset)
                .append(":").append(length)
                .append(":").append(text.replaceAll("\n", "$n"))
                .toString();
        writer.println(msg);
    }

    public UUID getUuid()
    {
        return uuid;
    }
}
