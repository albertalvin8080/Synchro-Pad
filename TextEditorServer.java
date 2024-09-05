import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;

// javac TextEditorServer.java && java TextEditorServer
public class TextEditorServer
{
    public static final short OP_BREAK = -1;
    public static final short OP_NEW_REQUEST = 10;
    public static final short OP_NEW_RESPONSE = 11;
    public static final short OP_NEW_RESPONSE_END = 12;
    public static final short OP_NEW_REQUEST_INIT = 13;
    public static final short OP_INSERT = 1;
    public static final short OP_DELETE = 2;

    public static final String SHARED_FILE = "./shared_file.txt";
    private static StringBuilder localText;
    private static volatile boolean running;

    public static void main(String[] args)
    {
        localText = readFromSharedFile();

        running = true;
        final Thread thread = new Thread(() -> {
            while (running)
            {
                try
                {
                    Thread.sleep(Duration.ofMinutes(1));
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
            while (true)
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
                Short operationType = Short.parseShort(parts[1]);
                int offset = Integer.parseInt(parts[2]);
                int length = Integer.parseInt(parts[3]);
                String text = parts[4];

                if (operationType == OP_NEW_REQUEST)
                {
                    var initPacket = constructPackage(senderId, OP_NEW_REQUEST_INIT, offset, length, "0", mcastaddr);
                    s.send(initPacket);
                    sendResponseInChunks(senderId, mcastaddr, s);
                }
                else if (operationType == OP_INSERT || operationType == OP_DELETE)
                {
                    localText.replace(offset, offset + length, text.equals("null") ? "" : text);
                    System.out.println(localText.toString());
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

    private static void sendResponseInChunks(String senderId, InetAddress mcastaddr, MulticastSocket s) throws IOException
    {
        byte[] textBytes = localText.toString().getBytes();
        int chunkSize = 4800;
        int totalChunks = (int) Math.ceil((double) textBytes.length / chunkSize);

        for (int i = 0; i < totalChunks; i++)
        {
            System.out.println(i);
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, textBytes.length);
            byte[] chunk = Arrays.copyOfRange(textBytes, start, end);

            // Check if this is the last chunk
            short operationType = (i == totalChunks - 1) ? OP_NEW_RESPONSE_END : OP_NEW_RESPONSE;

            // Construct and send the packet for this chunk
            DatagramPacket dOut = constructPackage(senderId, operationType, i, totalChunks, new String(chunk), mcastaddr);
            s.send(dOut);

            try{
                // Prevents from sending packets too early.
                Thread.sleep(200);
            } catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private static DatagramPacket constructPackage(String senderId, short operationType, int offset, int length, String text, InetAddress mcastaddr)
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

    private synchronized static void writeToSharedFile(StringBuilder localText)
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
