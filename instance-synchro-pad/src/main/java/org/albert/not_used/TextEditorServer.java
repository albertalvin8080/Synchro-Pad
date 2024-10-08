//package org.albert.util;
//
//import java.io.*;
//import java.net.*;
//import java.nio.file.Files;
//import java.time.Duration;
//
//public class TextEditorServer
//{
//    public static final short OP_BREAK = -1;
//    public static final short OP_NEW_REQUEST = 10;
//    public static final short OP_NEW_RESPONSE = 11;
//    public static final short OP_INSERT = 1;
//    public static final short OP_DELETE = 2;
//
//    private static StringBuilder localText;
//
//    public static void main(String[] args)
//    {
//        localText = readFromSharedFile();
//        final Thread thread = new Thread(() -> {
//            try
//            {
////                Thread.sleep(Duration.ofMinutes(1));
//                Thread.sleep(Duration.ofSeconds(5_000)); // Debug
//                writeToSharedFile(localText);
//                logger.info("Written");
//            }
//            catch (InterruptedException e)
//            {
//                throw new RuntimeException(e);
//            }
//        });
//
//        thread.start();
//
//        try
//        {
//            InetAddress mcastaddr = InetAddress.getByName("229.1.1.1");
//            InetSocketAddress group = new InetSocketAddress(mcastaddr, 1234);
//            NetworkInterface netIf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
//            MulticastSocket s = new MulticastSocket(1234);
//            s.joinGroup(group, netIf);
//
//            byte[] buf = new byte[1000];
//            DatagramPacket dIn = new DatagramPacket(buf, buf.length);
//            while (true)
//            {
//                logger.info("Server listening...");
//                s.receive(dIn);
//                String str = new String(buf, 0, dIn.getLength());
//                /*
//                 * [0] -> UUID (string)
//                 * [1] -> Operation Type
//                 * [2] -> offset
//                 * [3] -> length
//                 * [4] -> text from JTextArea
//                 * */
//                String[] parts = str.split(":", 5);
//                String senderId = parts[0];
//                Short operationType = Short.parseShort(parts[1]);
//                int offset = Integer.parseInt(parts[2]);
//                int length = Integer.parseInt(parts[3]);
//                String text = parts[4];
//
//                if (operationType == OP_NEW_REQUEST)
//                {
//                    byte[] msg = new StringBuilder(senderId)
//                            .append(":").append(OP_NEW_RESPONSE)
//                            .append(":").append(0)
//                            .append(":").append(0)
//                            .append(":").append(localText.toString())
//                            .toString()
//                            .getBytes();
//
//                    DatagramPacket dOut = new DatagramPacket(msg, msg.length, mcastaddr, 1234);
//                    s.send(dOut);
//                }
//                else if (operationType == OP_INSERT || operationType == OP_DELETE)
//                {
//                    localText.replace(offset, offset + length, text.equals("null") ? "" : text);
//                }
//                else if (operationType == OP_BREAK) // Just for the compiler to shut up
//                {
//                    break;
//                }
//            }
//
//            s.leaveGroup(group, netIf);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            writeToSharedFile(localText);
//        }
//
//        try
//        {
//            thread.join();
//        }
//        catch (InterruptedException e)
//        {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static StringBuilder readFromSharedFile()
//    {
//        File file = new File("./shared_file.txt");
//        if(!file.exists())
//        {
//            try
//            {
//                Files.createFile(file.toPath());
//            }
//            catch (IOException e)
//            {
//                throw new RuntimeException(e);
//            }
//        }
//
//        try (FileReader fr = new FileReader(file);
//             BufferedReader br = new BufferedReader(fr))
//        {
//            StringBuilder content = new StringBuilder();
//            String line;
//            while ((line = br.readLine()) != null)
//            {
//                content.append(line).append("\n"); // Avoiding Windows's "\r\n"
//            }
//            return content;
//        }
//        catch (IOException ex)
//        {
//            throw new RuntimeException(ex);
//        }
//    }
//
//    private synchronized static void writeToSharedFile(StringBuilder localText)
//    {
//        File file = new File("./shared_file.txt");
//
//        try (FileWriter fr = new FileWriter(file);
//             BufferedWriter bw = new BufferedWriter(fr))
//        {
//            String content = localText.toString();
//            // This converts '\n' to the platform's line separator
//            content = content.replaceAll("\n", System.lineSeparator());
//            bw.write(content);
//        }
//        catch (IOException ex)
//        {
//            throw new RuntimeException(ex);
//        }
//    }
//}
