package org.albert.server.udp;

import org.albert.util.SharedFileUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class SynchroPadServerUdp
{
    private StringBuilder sb;
    private boolean running = true;

    public void init()
    {
        sb = SharedFileUtils.readFromSharedFile();
        final Thread writeToSharedFileThread = createWriteToSharedFileThread();
        writeToSharedFileThread.start();

        System.out.println("Starting UDP server...");
        try (DatagramSocket socket = new DatagramSocket(1234))
        {
            List<ThreadedPadHandlerUdp> allHandlers = new ArrayList<>();
            byte[] buffer = new byte[1024];
            while (running)
            {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                System.out.println("Received connection...");
                ThreadedPadHandlerUdp handler = new ThreadedPadHandlerUdp(socket, packet.getAddress(), packet.getPort(), allHandlers, sb);
                handler.start();
                allHandlers.add(handler);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private Thread createWriteToSharedFileThread()
    {
        return new Thread(() -> {
            while (running)
            {
                try
                {
                    Thread.sleep(3000);
                    SharedFileUtils.writeToSharedFile(sb);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
