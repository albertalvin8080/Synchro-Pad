package org.albert.design_patterns.observer.udp;

import org.albert.util.CompilerProperties;
import org.albert.design_patterns.observer.DataSharer;
import org.albert.util.MessageHolder;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.UUID;

public class DataSharerUdp implements DataSharer
{
    private final UUID uuid;
    private final JTextArea textArea;
    private final DatagramSocket socket;
    private final InetAddress serverAddress;
    private volatile boolean running;
    private final Thread thread;

    public DataSharerUdp(UUID uuid, JTextArea textArea, DatagramSocket socket, InetAddress serverAddress)
    {
        this.uuid = uuid;
        this.textArea = textArea;
        this.socket = socket;
        this.serverAddress = serverAddress;

        running = true;
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
                    MessageHolder msgHolder = receiveMessage();
                    handleInsertOrDelete(msgHolder);
                }
                catch (SocketTimeoutException e)
                {
                    running = false;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    running = false;
                }
            }
        });
    }

    private MessageHolder receiveMessage() throws IOException, ClassNotFoundException
    {
        byte[] receiveData = new byte[1024];  // Adjust buffer size as needed
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        ByteArrayInputStream byteStream = new ByteArrayInputStream(receivePacket.getData());
        ObjectInputStream in = new ObjectInputStream(byteStream);
        return (MessageHolder) in.readObject();
    }

    @Override
    public void onInsert(int offset, int length, String text) throws IOException
    {
        MessageHolder msgHolder = new MessageHolder(uuid.toString(), OP_INSERT, offset, length, text);
        sendMessage(msgHolder);
    }

    @Override
    public void onDelete(int offset, int length, String text) throws IOException
    {
        MessageHolder msgHolder = new MessageHolder(uuid.toString(), OP_DELETE, offset, length, text);
        sendMessage(msgHolder);
    }

    private void sendMessage(MessageHolder msgHolder) throws IOException
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteStream);
        out.writeObject(msgHolder);
        out.flush();
        byte[] sendData = byteStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, 1234);
        socket.send(sendPacket);
    }

    @Override
    public void destroy()
    {
        running = false;
        thread.interrupt();
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
}
