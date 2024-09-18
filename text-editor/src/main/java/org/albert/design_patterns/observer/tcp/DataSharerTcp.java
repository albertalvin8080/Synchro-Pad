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
    private volatile boolean running;
    private final Thread thread;
    private final ObjectInputStream reader;
    private final ObjectOutputStream writer;

    public DataSharerTcp(UUID uuid, JTextArea textArea, ObjectInputStream reader, ObjectOutputStream writer) throws IOException, ClassNotFoundException
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
                    text = text == null ? "" : text;

                    final StringBuilder sb = new StringBuilder(textArea.getText());
                    if (operationType == DataSharer.OP_INSERT ||
                            operationType == DataSharer.OP_DELETE)
                    {
                        final int offSetPlusLength = offset + length;
                        sb.replace(offset, offSetPlusLength, text);
                        final int oldCaretPos = textArea.getCaretPosition();
                        textArea.setText(sb.toString());
                        textArea.setCaretPosition(oldCaretPos);

                        System.out.println("OLD CARET:   " + oldCaretPos);
                        System.out.println("Offset:      " + offset);
                        System.out.println("Length:      " + length);
                        System.out.println("Text:        " + text);
                        System.out.println("Text length: " + text.length());
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

    @Override
    public void destroy()
    {
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
}
