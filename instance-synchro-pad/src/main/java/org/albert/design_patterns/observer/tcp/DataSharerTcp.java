package org.albert.design_patterns.observer.tcp;

import org.albert.util.CompilerProperties;
import org.albert.design_patterns.observer.DataSharer;
import org.albert.util.MessageHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

public class DataSharerTcp implements DataSharer
{
    private static final Logger logger = LoggerFactory.getLogger(DataSharerTcp.class);

    private final Socket permissionSocket;
    private final UUID uuid;
    private final JTextArea textArea;
    private final AbstractDocument abstractDocument;
    private volatile boolean running;
    private final Thread thread;
    private final ObjectInputStream reader;
    private final ObjectOutputStream writer;
    private final ObjectInputStream readerPermission;
    private final ObjectOutputStream writerPermission;

    public DataSharerTcp(String serverIP, UUID uuid, JTextArea textArea, ObjectInputStream reader, ObjectOutputStream writer) throws IOException, ClassNotFoundException
    {
        this.uuid = uuid;
        this.abstractDocument = (AbstractDocument) textArea.getDocument();
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
                        logger.info("CreateAsyncThread waiting for server");
                    MessageHolder msgHolder = (MessageHolder) reader.readObject();

                    short operationType = msgHolder.getOperationType();
                    if (CompilerProperties.DEBUG)
                        logger.info("CreateAsyncThread received operationType: " + operationType);

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
                    logger.info("{}", e.getStackTrace());
                    running = false;
                }
                catch (EOFException e)
                {
                    // Occurs when the server closes, for example.
                    logger.info("{}", e.getStackTrace());
                    destroy();
                }
                catch (Exception e)
                {
                    if (CompilerProperties.DEBUG)
                        throw new RuntimeException(e);
                    else
                        logger.info("{}", e.getStackTrace());
                }
            }
        });
    }

    public void handleInsertOrDelete(MessageHolder msgHolder) throws BadLocationException
    {
        int offset = msgHolder.getOffset();
        int length = msgHolder.getLength();
        String text = msgHolder.getText();
        text = text == null ? "" : text;

//        final StringBuilder sb = new StringBuilder(textArea.getText());
//        final int offSetPlusLength = offset + length;
        abstractDocument.replace(offset, length, text, null);
//        final int oldCaretPos = textArea.getCaretPosition();
//        textArea.setText(sb.toString());
//        textArea.setCaretPosition(oldCaretPos);

        if (CompilerProperties.DEBUG)
        {
//            logger.info("OLD CARET:   " + oldCaretPos);
            logger.info("Offset:      " + offset);
            logger.info("Length:      " + length);
            logger.info("Text:        " + text);
            logger.info("Text length: " + text.length());
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
                logger.info("{}", e.getStackTrace());
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
                logger.info("WAITING FOR WRITE PERMISSION");

            final MessageHolder response = (MessageHolder) readerPermission.readObject();

            if (CompilerProperties.DEBUG)
                logger.info("PERMISSION RECEIVED: {}",
                    response.getOperationType() == DataSharer.OP_ACCEPTED_GLOBAL_WRITE
                    ? "accepted" : "rejected"
                );
            return response.getOperationType() == DataSharer.OP_ACCEPTED_GLOBAL_WRITE;
        }
        catch (IOException | ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
    // !============== SERVER SYNCHRONIZE ==============
}