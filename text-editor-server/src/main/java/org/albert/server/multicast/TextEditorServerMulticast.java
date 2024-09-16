package org.albert.server.multicast;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;

// javac TextEditorServer.java && java TextEditorServer
public class TextEditorServerMulticast
{
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
        localText = readFromSharedFile();

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
            System.out.println("Server listening...");
            while (running)
            {
                s.receive(dIn);
                System.out.println("Package received.");

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

                System.out.println(Arrays.toString(parts));

                if (operationType == OP_NEW_REQUEST)
                {
                    var initPacket = constructPackage(senderId, OP_NEW_REQUEST_INIT, 0, 0, "0", mcastaddr);
                    s.send(initPacket);
                    sendResponseInChunks(senderId, mcastaddr, s);
                }
                else if (operationType == OP_INSERT || operationType == OP_DELETE)
                {
                    localText.replace(offset, offset + length, text.equals("null") ? "" : text);
//                    System.out.println(localText.toString());
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
            e.printStackTrace();
        }
        finally
        {
            writeToSharedFile(localText);
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
                    writeToSharedFile(localText);
//                    System.out.println("Written"); // Debug
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

    private static StringBuilder readFromSharedFile()
    {
        File file = new File(SHARED_FILE);
        if (!file.exists())
        {
            try
            {
                Files.createFile(file.toPath());
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr))
        {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
            {
                content.append(line).append("\n"); // Avoiding Windows's "\r\n"
            }
            return content;
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
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
            System.out.println(i);
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

    private synchronized void writeToSharedFile(StringBuilder localText)
    {
        File file = new File(SHARED_FILE);

        try (FileWriter fr = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fr))
        {
            String content = localText.toString();
            // This converts '\n' to the platform's line separator
            content = content.replaceAll("\n", System.lineSeparator());
            bw.write(content);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
