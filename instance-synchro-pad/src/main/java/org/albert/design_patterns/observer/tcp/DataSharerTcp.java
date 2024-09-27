package org.albert.design_patterns.observer.tcp;

import org.albert.util.CompilerProperties;
import org.albert.design_patterns.observer.DataSharer;
import org.albert.util.MessageHolder;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

public class DataSharerTcp implements DataSharer
{
    private final Object writeMonitor = new Object(); // Monitor object for synchronization

    private final Socket permissionSocket;
    private final UUID uuid;
    private final JTextArea textArea;
    private volatile boolean running;
    private final Thread thread;
    private final ObjectInputStream reader;
    private final ObjectOutputStream writer;
    private final ObjectInputStream readerPermission;
    private final ObjectOutputStream writerPermission;

    public DataSharerTcp(String serverIP, UUID uuid, JTextArea textArea, ObjectInputStream reader, ObjectOutputStream writer) throws IOException, ClassNotFoundException
    {
        this.uuid = uuid;
        this.textArea = textArea;
        this.reader = reader;
        this.writer = writer;

        running = true;

        // Must request the text from the server
        final MessageHolder msgHolder = new MessageHolder(
                uuid.toString(),
                OP_INIT_GLOBAL,
                0,
                0,
                ""
        );
        writer.writeObject(msgHolder);
        writer.flush();

        MessageHolder initialMessage = (MessageHolder) reader.readObject();
        textArea.setText(initialMessage.getText() == null ? "" : initialMessage.getText());
        // initialMessage.getInfo() has the port of the globalTextSocket
        this.permissionSocket = new Socket(serverIP, Integer.parseInt(initialMessage.getInfo()));
        readerPermission = new ObjectInputStream(permissionSocket.getInputStream());
        writerPermission = new ObjectOutputStream(permissionSocket.getOutputStream());
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
                    if (CompilerProperties.DEBUG)
                        System.out.println("CreateAsyncThread waiting for server");
                    MessageHolder msgHolder = (MessageHolder) reader.readObject();

                    short operationType = msgHolder.getOperationType();
                    if (CompilerProperties.DEBUG)
                        System.out.println("CreateAsyncThread received operationType: " + operationType);

                    if (operationType == DataSharer.OP_INSERT ||
                            operationType == DataSharer.OP_DELETE)
                    {
                        handleInsertOrDelete(msgHolder);
                    }
                }
                catch (IllegalArgumentException e) // Invalid caret position
                {
                    textArea.setCaretPosition(textArea.getText().length());
                }
                catch (SocketException e) // Socket closed
                {
                    System.out.println(e);
                    running = false;
                }
                catch (EOFException e)
                {
                    // Occurs when the server closes, for example.
                    System.out.println(e);
                    destroy();
                }
                catch (Exception e)
                {
                    if (CompilerProperties.DEBUG)
                        throw new RuntimeException(e);
                    else
                        e.printStackTrace();
                }
            }
        });
    }

    public void handleInsertOrDelete(MessageHolder msgHolder)
    {
        int offset = msgHolder.getOffset();
        int length = msgHolder.getLength();
        String text = msgHolder.getText();
        text = text == null ? "" : text;

        final StringBuilder sb = new StringBuilder(textArea.getText());
        final int offSetPlusLength = offset + length;
        sb.replace(offset, offSetPlusLength, text);
        final int oldCaretPos = textArea.getCaretPosition();
        textArea.setText(sb.toString());
        textArea.setCaretPosition(oldCaretPos);

        if (CompilerProperties.DEBUG)
        {
            System.out.println("OLD CARET:   " + oldCaretPos);
            System.out.println("Offset:      " + offset);
            System.out.println("Length:      " + length);
            System.out.println("Text:        " + text);
            System.out.println("Text length: " + text.length());
        }
    }

    @Override
    public void destroy()
    {
        try
        {
            final MessageHolder finalMsg = new MessageHolder(null, DataSharer.OP_DISCONNECT_GLOBAL, 0, 0, null);
            writer.writeObject(finalMsg);
        }
        catch (Exception e)
        {
            if (CompilerProperties.DEBUG)
                e.printStackTrace();
        }
        running = false;
        thread.interrupt();
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

    // ============== SERVER SYNCHRONIZE ==============
    public boolean requestWritePermission()
    {
        final MessageHolder msgHolder = new MessageHolder(
                uuid.toString(),
                DataSharer.OP_REQUEST_GLOBAL_WRITE,
                0,
                0,
                null
        );
        try
        {
            writerPermission.writeObject(msgHolder);
            writerPermission.flush();

            if (CompilerProperties.DEBUG)
                System.out.println("WAITING FOR WRITE PERMISSION");

            final MessageHolder response = (MessageHolder) readerPermission.readObject();

            if (CompilerProperties.DEBUG)
                System.out.println("PERMISSION RECEIVED");
            return response.getOperationType() == DataSharer.OP_ACCEPTED_GLOBAL_WRITE;
        }
        catch (IOException | ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
    // !============== SERVER SYNCHRONIZE ==============
}