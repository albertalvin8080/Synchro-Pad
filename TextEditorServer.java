import java.net.*;
import java.util.*;

public class TextEditorServer
{
    public static final short OP_BREAK = -1;
    public static final short OP_NEW_REQUEST = 10;
    public static final short OP_NEW_RESPONSE = 11;
    public static final short OP_INSERT = 1;
    public static final short OP_DELETE = 2;

    public static void main(String[] args)
    {
        Scanner sc = new Scanner(System.in);
        StringBuilder localText = new StringBuilder("");

        try
        {
            InetAddress mcastaddr = InetAddress.getByName("229.1.1.1");
            InetSocketAddress group = new InetSocketAddress(mcastaddr, 1234);
            NetworkInterface netIf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            MulticastSocket s = new MulticastSocket(1234);
            s.joinGroup(group, netIf);

            byte[] buf = new byte[1000];
            DatagramPacket dIn = new DatagramPacket(buf, buf.length);
            while (true)
            {
                System.out.println("Server listening...");
                s.receive(dIn);
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
                    byte[] msg = new StringBuilder(senderId)
                            .append(":").append(OP_NEW_RESPONSE)
                            .append(":").append(0)
                            .append(":").append(0)
                            .append(":").append(localText.toString())
                            .toString()
                            .getBytes();

                    DatagramPacket dOut = new DatagramPacket(msg, msg.length, mcastaddr, 1234);
                    s.send(dOut);
                }
                else if (operationType == OP_INSERT)
                {
                    localText.insert(offset, text);
                }
                else if (operationType == OP_DELETE)
                {
                    localText.delete(offset, offset + length);
                }
                else if(operationType == OP_BREAK) // Just for the compiler to shut up
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
            sc.close();
        }
    }
}
