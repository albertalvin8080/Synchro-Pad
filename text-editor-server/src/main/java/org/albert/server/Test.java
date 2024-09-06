//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.net.MulticastSocket;
//import java.net.NetworkInterface;
//
//private Thread createNewRequestThread(String senderId)
//{
//    Thread t = new Thread(() -> {
//        MulticastSocket _multicastSocket = null;
//        InetAddress _mcastaddr = null;
//        NetworkInterface _netIf = null;
//        InetSocketAddress _group = null;
//        try
//        {
//            _mcastaddr = InetAddress.getByName("229.2.2.2");
//            _netIf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
//            final int _port = 1234;
//            _group = new InetSocketAddress(_mcastaddr, _port);
//            _multicastSocket = new MulticastSocket(_port);
//            _multicastSocket.joinGroup(_group, _netIf);
//
//            var initPacket = constructPackage(senderId, OP_NEW_REQUEST_INIT, 0, 0, "0", _mcastaddr);
//            _multicastSocket.send(initPacket);
//            sendResponseInChunks(senderId, _mcastaddr,  _multicastSocket);
//        }
//        catch (IOException e)
//        {
//            throw new RuntimeException(e);
//        }
//        finally
//        {
//            if(_multicastSocket != null)
//            {
//                try
//                {
//                    _multicastSocket.leaveGroup(_group, _netIf);
//                }
//                catch (IOException e)
//                {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//    });
//
//    t.start();
//    return t;
//}