package org.albert.server.multicast;

import org.albert.util.SharedFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.Arrays;

// javac TextEditorServer.java && java TextEditorServer
public class SynchroPadServerMulticast
{
    private static final Logger logger = LoggerFactory.getLogger(SynchroPadServerMulticast.class);

    public static final short OP_BREAK = -1;
    public static final short OP_INSERT = 1;
    public static final short OP_DELETE = 2;
    public static final short OP_NEW_REQUEST = 10;
    public static final short OP_NEW_RESPONSE = 11;
    public static final short OP_NEW_RESPONSE_END = 12;
    public static final short OP_NEW_REQUEST_INIT = 13;
    public static final short OP_NEW_REQUEST_RECEIVED = 14;
    public static final String SHARED_FILE = "./shared_file.txt";

    private StringBuilder localText;
    private volatile boolean running;

    public void init()
    {
        localText = SharedFileUtils.readFromSharedFile();

        running = true;
        final Thread thread = createWriteToSharedFileThread();

        try
        {
            InetAddress mcastaddr = InetAddress.getByName("229.1.1.1");
            InetSocketAddress group = new InetSocketAddress(mcastaddr, 1234);
            NetworkInterface netIf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            MulticastSocket s = new MulticastSocket(1234);
            s.joinGroup(group, netIf);

            byte[] buf = new byte[5000];
            DatagramPacket dIn = new DatagramPacket(buf, buf.length);
            logger.info("Server listening...");
            while (running)
            {
                s.receive(dIn);
                logger.info("Package received.");

                String str = new String(buf, 0, dIn.getLength());
                /*
                 * [0] -> UUID (string)
                 * [1] -> Operation Type
                 * [2] -> offset
                 * [3] -> length
                 * [4] -> text from JTextArea
                 * */
                String[] parts = str.split(":", 5);
                String senderId = parts[0];
                short operationType = Short.parseShort(parts[1]);
                int offset = Integer.parseInt(parts[2]);
                int length = Integer.parseInt(parts[3]);
                String text = parts[4];

                logger.info(Arrays.toString(parts));

                if (operationType == OP_NEW_REQUEST)
                {
                    var initPacket = constructPackage(senderId, OP_NEW_REQUEST_INIT, 0, 0, "0", mcastaddr);
                    s.send(initPacket);
                    sendResponseInChunks(senderId, mcastaddr, s);
                }
                else if (operationType == OP_INSERT || operationType == OP_DELETE)
                {
                    localText.replace(offset, offset + length, text.equals("null") ? "" : text);
//                    logger.info(localText.toString());
                }
                else if (operationType == OP_BREAK) // Just for the compiler to shut up
                {
                    break;
                }
            }

            s.leaveGroup(group, netIf);
        }
        catch (Exception e)
        {
            logger.error("{}", e.getStackTrace());
        }
        finally
        {
            SharedFileUtils.writeToSharedFile(localText);
            running = false;
        }

        try
        {
            thread.join();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Thread createWriteToSharedFileThread()
    {
        final Thread thread = new Thread(() -> {
            while (running)
            {
                try
                {
                    Thread.sleep(Duration.ofMinutes(1).toMillis());
//                    Thread.sleep(Duration.ofSeconds(3)); // Debug
                    SharedFileUtils.writeToSharedFile(localText);
//                    logger.info("Written"); // Debug
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        thread.start();
        return thread;
    }

    private void sendResponseInChunks(String senderId, InetAddress mcastaddr, MulticastSocket s) throws IOException
    {
        byte[] textBytes = localText.toString().getBytes();
        int chunkSize = 4800;
        int totalChunks = (int) Math.ceil((double) textBytes.length / chunkSize);

        int i, start, end;
        byte[] chunk;
        for (i = 0; i < totalChunks - 1; i++)
        {
            logger.info("{}", i);
            start = i * chunkSize;
            end = Math.min(start + chunkSize, textBytes.length);
            chunk = Arrays.copyOfRange(textBytes, start, end);

            // Construct and send the packet for this chunk
            DatagramPacket dOut = constructPackage(senderId, OP_NEW_RESPONSE, 0, 0, new String(chunk), mcastaddr);
            s.send(dOut);

            byte[] buf = new byte[5000];
            DatagramPacket dIn = new DatagramPacket(buf, buf.length);
            while(true)
            {
                s.receive(dIn);
                String str = new String(buf, 0, dIn.getLength());
                /*
                 * [0] -> UUID (string)
                 * [1] -> Operation Type
                 * */
                String[] parts = str.split(":", 5);
                String _senderId = parts[0];
                short operationType = Short.parseShort(parts[1]);
                if(_senderId.equals(senderId) && operationType == OP_NEW_REQUEST_RECEIVED)
                    break;
            }
        }

        // OP_NEW_RESPONSE_END
        {
            start = i * chunkSize;
            end = Math.min(start + chunkSize, textBytes.length);
            chunk = Arrays.copyOfRange(textBytes, start, end);

            DatagramPacket dOut = constructPackage(senderId, OP_NEW_RESPONSE_END, 0, 0, new String(chunk), mcastaddr);
            s.send(dOut);
        }
    }

    private DatagramPacket constructPackage(String senderId, short operationType, int offset, int length, String text, InetAddress mcastaddr)
    {
        byte[] msg = new StringBuilder(senderId)
                .append(":").append(operationType)
                .append(":").append(offset)
                .append(":").append(length)
                .append(":").append(text)
                .toString()
                .getBytes();

        return new DatagramPacket(msg, msg.length, mcastaddr, 1234);
    }
}
