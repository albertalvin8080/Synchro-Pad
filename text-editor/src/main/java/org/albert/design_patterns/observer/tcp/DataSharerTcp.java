package org.albert.design_patterns.observer.tcp;

import org.albert.design_patterns.observer.DataSharer;
import org.albert.util.MessageHolder;

import javax.swing.*;
import java.io.*;
import java.lang.ref.Cleaner;
import java.net.Socket;
import java.net.SocketException;
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

    public DataSharerTcp(JTextArea textArea, String serverIp) throws IOException, ClassNotFoundException
    {
        this.textArea = textArea;
        initializeNetworking(serverIp);

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
        textArea.setText(initialMessage.getText() == null ? "" : initialMessage.getText());
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
//                    System.out.println("Received: " + msgHolder.getText());

                    short operationType = msgHolder.getOperationType();
                    int offset = msgHolder.getOffset();
                    int length = msgHolder.getLength();
                    String text = msgHolder.getText();

                    final StringBuilder sb = new StringBuilder(textArea.getText());
                    if (operationType == DataSharer.OP_INSERT ||
                            operationType == DataSharer.OP_DELETE)
                    {
                        final int offSetPlusLength = offset + length;
                        sb.replace(offset, offSetPlusLength, text == null ? "" : text);
                        final int oldCaretPosition = textArea.getCaretPosition();
                        textArea.setText(sb.toString());
                        System.out.println("OLD CARET: " + oldCaretPosition);
                        System.out.println("Length: " + length);
                        System.out.println("Offset: " + offset);
                        if(oldCaretPosition < offSetPlusLength)
                            textArea.setCaretPosition(oldCaretPosition);
                        else if (oldCaretPosition > offSetPlusLength)
                        {
                            textArea.setCaretPosition(oldCaretPosition + length);
                        }
                    }
                }
                catch (IllegalArgumentException e) // Invalid caret position
                {
                    textArea.setCaretPosition(textArea.getText().length());
                }
                catch (SocketException e) // Socket closed
                {
                    System.out.println(e);
                }
                catch (EOFException e)
                {
                    // Occurs when the server closes, for example.
                    System.out.println(e);
                    destroy();
                }
                catch (IOException | ClassNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void initializeNetworking(String serverIp) throws IOException
    {
        final int port = 1234;
        socket = new Socket(serverIp, port);
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
    public void onInsert(int offset, int length, String text) throws IOException
    {
        MessageHolder msgHolder = new MessageHolder(uuid.toString(), OP_INSERT, offset, length, text);
        writer.writeObject(msgHolder);
        writer.flush();
    }

    @Override
    public void onDelete(int offset, int length, String text) throws IOException
    {
        MessageHolder msgHolder = new MessageHolder(uuid.toString(), OP_DELETE, offset, length, text);
        writer.writeObject(msgHolder);
        writer.flush();
    }

    public UUID getUuid()
    {
        return uuid;
    }
}
